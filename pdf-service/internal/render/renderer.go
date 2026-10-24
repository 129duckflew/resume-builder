package render

import (
	"context"
	"errors"
	"fmt"
	"log/slog"
	"sync"
	"sync/atomic"
	"time"

	"github.com/129duckflew/resume-builder/pdf-service/internal/config"
)

var (
	// ErrUnavailable indicates that no render worker is currently ready.
	ErrUnavailable = errors.New("renderer unavailable")
	// ErrQueueFull indicates that the bounded render queue has no capacity.
	ErrQueueFull = errors.New("render queue full")
)

// Health describes whether the renderer can accept work.
type Health struct {
	Ready  bool
	Reason string
}

// Renderer converts HTML to PDF bytes or a rendered document height.
type Renderer interface {
	Measure(context.Context, string) (float64, error)
	PDF(context.Context, string) ([]byte, error)
	Health() Health
}

type jobKind uint8

const (
	jobMeasure jobKind = iota
	jobPDF
)

type job struct {
	ctx    context.Context
	kind   jobKind
	html   string
	result chan jobResult
}

type jobResult struct {
	pdf    []byte
	height float64
	err    error
}

// workQueue lets a cancelled queued request return its capacity immediately
// instead of waiting for a worker to dequeue it.
type workQueue struct {
	mu       sync.Mutex
	items    []*job
	capacity int
	waiting  int
	closed   bool
	wake     chan struct{}
}

func newWorkQueue(capacity int) *workQueue {
	return &workQueue{
		capacity: capacity,
		wake:     make(chan struct{}, 1),
	}
}

func (q *workQueue) push(req *job) error {
	q.mu.Lock()
	defer q.mu.Unlock()

	if q.closed {
		return ErrUnavailable
	}
	if q.capacity == 0 {
		// With no queue capacity, only an already-waiting worker may take
		// the request directly.
		if len(q.items) >= q.waiting {
			return ErrQueueFull
		}
	} else if len(q.items) >= q.capacity {
		return ErrQueueFull
	}
	q.items = append(q.items, req)
	q.signalLocked()
	return nil
}

func (q *workQueue) pop(done <-chan struct{}) (*job, bool) {
	for {
		q.mu.Lock()
		if len(q.items) > 0 {
			req := q.items[0]
			copy(q.items, q.items[1:])
			q.items[len(q.items)-1] = nil
			q.items = q.items[:len(q.items)-1]
			q.mu.Unlock()
			return req, true
		}
		if q.closed {
			q.mu.Unlock()
			return nil, false
		}

		q.waiting++
		q.mu.Unlock()
		select {
		case <-q.wake:
		case <-done:
			q.mu.Lock()
			q.waiting--
			q.mu.Unlock()
			return nil, false
		}
		q.mu.Lock()
		q.waiting--
		q.mu.Unlock()
	}
}

func (q *workQueue) remove(req *job) {
	q.mu.Lock()
	defer q.mu.Unlock()
	for index, item := range q.items {
		if item != req {
			continue
		}
		copy(q.items[index:], q.items[index+1:])
		q.items[len(q.items)-1] = nil
		q.items = q.items[:len(q.items)-1]
		return
	}
}

func (q *workQueue) close() {
	q.mu.Lock()
	q.closed = true
	q.items = nil
	q.signalLocked()
	q.mu.Unlock()
}

func (q *workQueue) signalLocked() {
	select {
	case q.wake <- struct{}{}:
	default:
	}
}

type generation struct {
	browserCtx context.Context
	cancel     context.CancelFunc
	queue      *workQueue
	healthTab  *renderTab
	done       chan struct{}
	doneOnce   sync.Once
	workers    sync.WaitGroup
}

func (g *generation) stop() {
	g.doneOnce.Do(func() {
		g.queue.close()
		close(g.done)
		g.cancel()
		if g.healthTab != nil {
			g.healthTab.cancel()
		}
	})
}

// browserDriver isolates CDP so queue and lifecycle behavior can be tested
// without launching Chromium.
type browserDriver interface {
	Launch(context.Context) error
	Probe(context.Context) error
	NewTab(context.Context, config.Config) (*renderTab, error)
	Load(context.Context, string) error
	Measure(context.Context) (float64, error)
	PrintPDF(context.Context) ([]byte, error)
}

// Manager owns one Chromium process and a bounded pool of reusable render tabs.
type Manager struct {
	cfg    config.Config
	logger *slog.Logger
	driver browserDriver

	ctx       context.Context
	cancel    context.CancelFunc
	restartCh chan string
	stopped   chan struct{}
	closeOnce sync.Once

	mu  sync.RWMutex
	gen *generation

	healthProbeMu sync.Mutex
	healthMu      sync.RWMutex
	health        Health

	requests     atomic.Uint64
	shuttingDown atomic.Bool
}

// NewManager starts the browser supervisor. Readiness becomes true once the
// Chromium process and all configured render tabs are initialized.
func NewManager(parent context.Context, cfg config.Config, logger *slog.Logger) *Manager {
	return newManager(parent, cfg, logger, cdpDriver{})
}

func newManager(parent context.Context, cfg config.Config, logger *slog.Logger, driver browserDriver) *Manager {
	if logger == nil {
		logger = slog.Default()
	}
	ctx, cancel := context.WithCancel(parent)
	m := &Manager{
		cfg:       cfg,
		logger:    logger,
		driver:    driver,
		ctx:       ctx,
		cancel:    cancel,
		restartCh: make(chan string, 1),
		stopped:   make(chan struct{}),
		health:    Health{Reason: "browser starting"},
	}
	go m.supervise()
	return m
}

// BeginShutdown prevents new render jobs while allowing already accepted
// requests to finish before Close is called.
func (m *Manager) BeginShutdown() {
	m.shuttingDown.Store(true)
	m.setHealth(false, "shutting down")
}

// Close stops the supervisor, workers, and Chromium process.
func (m *Manager) Close() error {
	m.closeOnce.Do(func() {
		m.BeginShutdown()
		m.cancel()
		<-m.stopped
	})
	return nil
}

// Health returns the current readiness state.
func (m *Manager) Health() Health {
	m.healthMu.RLock()
	defer m.healthMu.RUnlock()
	return m.health
}

// Measure renders HTML and returns document.body.scrollHeight.
func (m *Manager) Measure(ctx context.Context, html string) (float64, error) {
	result, err := m.submit(ctx, jobMeasure, html)
	return result.height, err
}

// PDF renders HTML and returns PDF bytes.
func (m *Manager) PDF(ctx context.Context, html string) ([]byte, error) {
	result, err := m.submit(ctx, jobPDF, html)
	return result.pdf, err
}

func (m *Manager) submit(ctx context.Context, kind jobKind, html string) (jobResult, error) {
	if m.shuttingDown.Load() || !m.Health().Ready {
		return jobResult{}, ErrUnavailable
	}

	m.mu.RLock()
	gen := m.gen
	m.mu.RUnlock()
	if gen == nil {
		return jobResult{}, ErrUnavailable
	}

	req := &job{
		ctx:    ctx,
		kind:   kind,
		html:   html,
		result: make(chan jobResult, 1),
	}
	if err := gen.queue.push(req); err != nil {
		return jobResult{}, err
	}
	stopRemoval := context.AfterFunc(ctx, func() {
		gen.queue.remove(req)
	})
	defer stopRemoval()

	select {
	case result := <-req.result:
		return result, result.err
	case <-ctx.Done():
		return jobResult{}, ctx.Err()
	case <-gen.done:
		return jobResult{}, ErrUnavailable
	}
}

func (m *Manager) supervise() {
	defer close(m.stopped)

	backoff := time.Second
	for {
		if m.ctx.Err() != nil {
			m.setHealth(false, "shutting down")
			m.stopCurrentGeneration()
			return
		}

		if err := m.startGeneration(); err != nil {
			m.setHealth(false, err.Error())
			m.logger.Error("pdf browser generation failed", "error", err)
			if !m.wait(backoff) {
				return
			}
			backoff = nextBackoff(backoff)
			continue
		}

		backoff = time.Second
		ticker := time.NewTicker(m.cfg.BrowserHealthInterval)
		restarted := false
		for !restarted {
			select {
			case <-m.ctx.Done():
				ticker.Stop()
				m.setHealth(false, "shutting down")
				m.stopCurrentGeneration()
				return
			case reason := <-m.restartCh:
				ticker.Stop()
				m.logger.Warn("restarting pdf browser", "reason", reason)
				m.setHealth(false, "browser restarting")
				m.stopCurrentGeneration()
				restarted = true
			case <-ticker.C:
				if err := m.pingBrowser(); err != nil {
					ticker.Stop()
					m.logger.Warn("pdf browser health check failed", "error", err)
					m.setHealth(false, "browser health check failed")
					m.stopCurrentGeneration()
					restarted = true
				}
			}
		}

		if !m.wait(backoff) {
			return
		}
		backoff = nextBackoff(backoff)
	}
}

func (m *Manager) startGeneration() error {
	opts := append([]chromedpExecAllocatorOption{}, defaultBrowserOptions(m.cfg)...)
	allocatorCtx, allocatorCancel := newBrowserAllocator(m.ctx, opts...)
	browserCtx, browserCancel := newBrowserContext(allocatorCtx)

	if err := m.launchBrowser(browserCtx); err != nil {
		browserCancel()
		allocatorCancel()
		return fmt.Errorf("launch Chromium: %w", err)
	}

	gen := &generation{
		browserCtx: browserCtx,
		cancel: func() {
			browserCancel()
			allocatorCancel()
		},
		queue: newWorkQueue(m.cfg.QueueCapacity),
		done:  make(chan struct{}),
	}

	healthTab, err := m.driver.NewTab(browserCtx, m.cfg)
	if err != nil {
		gen.cancel()
		return fmt.Errorf("initialize health probe tab: %w", err)
	}
	gen.healthTab = healthTab

	tabs := make([]*renderTab, 0, m.cfg.MaxConcurrency)
	for i := 0; i < m.cfg.MaxConcurrency; i++ {
		tab, err := m.driver.NewTab(browserCtx, m.cfg)
		if err != nil {
			healthTab.cancel()
			for _, created := range tabs {
				created.cancel()
			}
			gen.cancel()
			return fmt.Errorf("initialize render tab %d: %w", i, err)
		}
		tabs = append(tabs, tab)
	}

	m.mu.Lock()
	m.gen = gen
	m.mu.Unlock()
	m.setHealth(true, "ready")
	m.logger.Info("pdf browser ready", "concurrency", m.cfg.MaxConcurrency, "queue_capacity", m.cfg.QueueCapacity)

	gen.workers.Add(len(tabs))
	for index, tab := range tabs {
		go m.runWorker(gen, index, tab)
	}
	return nil
}

func (m *Manager) stopCurrentGeneration() {
	m.mu.Lock()
	gen := m.gen
	m.gen = nil
	m.mu.Unlock()
	if gen == nil {
		return
	}

	gen.stop()
	done := make(chan struct{})
	go func() {
		gen.workers.Wait()
		close(done)
	}()

	select {
	case <-done:
	case <-time.After(m.cfg.ShutdownTimeout):
		m.logger.Warn("timed out waiting for render workers to stop")
	}
}

func (m *Manager) launchBrowser(browserCtx context.Context) error {
	done := make(chan error, 1)
	go func() {
		done <- m.driver.Launch(browserCtx)
	}()

	select {
	case err := <-done:
		return err
	case <-time.After(m.cfg.BrowserStartupTimeout):
		return fmt.Errorf("startup exceeded %s", m.cfg.BrowserStartupTimeout)
	case <-m.ctx.Done():
		return m.ctx.Err()
	}
}

func (m *Manager) pingBrowser() error {
	m.mu.RLock()
	gen := m.gen
	m.mu.RUnlock()
	if gen == nil || gen.healthTab == nil {
		return ErrUnavailable
	}

	m.healthProbeMu.Lock()
	defer m.healthProbeMu.Unlock()

	ctx, cancel := context.WithTimeout(gen.healthTab.ctx, 2*time.Second)
	defer cancel()
	return m.driver.Probe(ctx)
}

func (m *Manager) runWorker(gen *generation, index int, tab *renderTab) {
	defer gen.workers.Done()
	defer tab.cancel()

	jobsSinceRecycle := 0
	for {
		req, ok := gen.queue.pop(gen.done)
		if !ok {
			return
		}
		m.requests.Add(1)
		result := m.execute(gen, tab, *req)
		req.result <- result
		jobsSinceRecycle++

		if result.err == nil && jobsSinceRecycle < m.cfg.BrowserRecycleAfter {
			continue
		}

		tab.cancel()
		nextTab, err := m.driver.NewTab(gen.browserCtx, m.cfg)
		if err != nil {
			m.reportFailure(fmt.Sprintf("worker %d cannot recreate a tab: %v", index, err))
			return
		}
		tab = nextTab
		jobsSinceRecycle = 0
	}
}

func (m *Manager) execute(gen *generation, tab *renderTab, req job) jobResult {
	if err := req.ctx.Err(); err != nil {
		return jobResult{err: err}
	}

	ctx, cancel := context.WithTimeout(tab.ctx, m.cfg.RequestTimeout)
	stopCancel := context.AfterFunc(req.ctx, cancel)
	defer func() {
		stopCancel()
		cancel()
	}()

	started := time.Now()
	err := m.driver.Load(ctx, req.html)
	if err == nil && req.ctx.Err() != nil {
		err = req.ctx.Err()
	}
	if err != nil {
		if gen.doneClosed() {
			return jobResult{err: ErrUnavailable}
		}
		if req.ctx.Err() != nil {
			return jobResult{err: req.ctx.Err()}
		}
		return jobResult{err: fmt.Errorf("load document: %w", err)}
	}

	var result jobResult
	switch req.kind {
	case jobMeasure:
		result.height, result.err = m.driver.Measure(ctx)
	case jobPDF:
		result.pdf, result.err = m.driver.PrintPDF(ctx)
	default:
		result.err = fmt.Errorf("unknown render job kind %d", req.kind)
	}

	if result.err == nil {
		m.logger.Debug("render completed",
			"kind", req.kind,
			"duration_ms", time.Since(started).Milliseconds(),
			"bytes", len(result.pdf),
			"height", result.height,
		)
	}
	return result
}

func (m *Manager) reportFailure(reason string) {
	m.setHealth(false, reason)
	select {
	case m.restartCh <- reason:
	default:
	}
}

func (m *Manager) setHealth(ready bool, reason string) {
	if m.shuttingDown.Load() {
		ready = false
		reason = "shutting down"
	}
	m.healthMu.Lock()
	m.health = Health{Ready: ready, Reason: reason}
	m.healthMu.Unlock()
}

func (m *Manager) wait(duration time.Duration) bool {
	timer := time.NewTimer(duration)
	defer timer.Stop()
	select {
	case <-timer.C:
		return true
	case <-m.ctx.Done():
		return false
	}
}

func nextBackoff(current time.Duration) time.Duration {
	next := current * 2
	if next > 30*time.Second {
		return 30 * time.Second
	}
	return next
}
