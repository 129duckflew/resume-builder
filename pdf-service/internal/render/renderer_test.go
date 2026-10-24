package render

import (
	"context"
	"errors"
	"io"
	"log/slog"
	"sync"
	"testing"
	"time"

	"github.com/129duckflew/resume-builder/pdf-service/internal/config"
)

type fakeDriver struct {
	mu          sync.Mutex
	launchErr   error
	probeErr    error
	newTabErr   error
	newTabCalls int
	loadFn      func(context.Context, string) error
	measureFn   func(context.Context) (float64, error)
	pdfFn       func(context.Context) ([]byte, error)
}

func (d *fakeDriver) Launch(context.Context) error {
	d.mu.Lock()
	defer d.mu.Unlock()
	return d.launchErr
}

func (d *fakeDriver) Probe(context.Context) error {
	d.mu.Lock()
	defer d.mu.Unlock()
	return d.probeErr
}

func (d *fakeDriver) NewTab(ctx context.Context, _ config.Config) (*renderTab, error) {
	d.mu.Lock()
	defer d.mu.Unlock()
	if d.newTabErr != nil {
		return nil, d.newTabErr
	}
	if err := ctx.Err(); err != nil {
		return nil, err
	}
	d.newTabCalls++
	tabCtx, cancel := context.WithCancel(ctx)
	return &renderTab{ctx: tabCtx, cancel: cancel}, nil
}

func (d *fakeDriver) Load(ctx context.Context, html string) error {
	if d.loadFn != nil {
		return d.loadFn(ctx, html)
	}
	return nil
}

func (d *fakeDriver) Measure(ctx context.Context) (float64, error) {
	if d.measureFn != nil {
		return d.measureFn(ctx)
	}
	return 1123, nil
}

func (d *fakeDriver) PrintPDF(context.Context) ([]byte, error) {
	if d.pdfFn != nil {
		return d.pdfFn(context.Background())
	}
	return []byte("%PDF-test"), nil
}

func (d *fakeDriver) setProbeError(err error) {
	d.mu.Lock()
	d.probeErr = err
	d.mu.Unlock()
}

func (d *fakeDriver) tabCalls() int {
	d.mu.Lock()
	defer d.mu.Unlock()
	return d.newTabCalls
}

func testManager(t *testing.T, cfg config.Config, driver *fakeDriver) *Manager {
	t.Helper()
	logger := slog.New(slog.NewTextHandler(io.Discard, nil))
	m := newManager(context.Background(), cfg, logger, driver)
	t.Cleanup(func() {
		_ = m.Close()
	})
	return m
}

func testConfig() config.Config {
	cfg := config.Default()
	cfg.MaxConcurrency = 1
	cfg.QueueCapacity = 1
	cfg.RequestTimeout = 5 * time.Second
	cfg.BrowserStartupTimeout = time.Second
	cfg.ShutdownTimeout = time.Second
	cfg.BrowserHealthInterval = time.Hour
	cfg.BrowserRecycleAfter = 100
	return cfg
}

func waitHealth(t *testing.T, m *Manager, ready bool) {
	t.Helper()
	deadline := time.Now().Add(3 * time.Second)
	for time.Now().Before(deadline) {
		if m.Health().Ready == ready {
			return
		}
		time.Sleep(5 * time.Millisecond)
	}
	t.Fatalf("health did not become ready=%v: %+v", ready, m.Health())
}

func waitQueueLength(t *testing.T, m *Manager, want int) {
	t.Helper()
	deadline := time.Now().Add(3 * time.Second)
	for time.Now().Before(deadline) {
		m.mu.RLock()
		gen := m.gen
		m.mu.RUnlock()
		if gen != nil {
			gen.queue.mu.Lock()
			got := len(gen.queue.items)
			gen.queue.mu.Unlock()
			if got == want {
				return
			}
		}
		time.Sleep(5 * time.Millisecond)
	}
	t.Fatalf("queue length did not become %d", want)
}

func waitResult(t *testing.T, result <-chan error) error {
	t.Helper()
	select {
	case err := <-result:
		return err
	case <-time.After(5 * time.Second):
		t.Fatal("timed out waiting for render result")
		return nil
	}
}

func TestManagerBoundsQueueAndReleasesCancelledRequest(t *testing.T) {
	cfg := testConfig()
	cfg.QueueCapacity = 1

	started := make(chan struct{})
	release := make(chan struct{})
	var startOnce sync.Once
	driver := &fakeDriver{
		loadFn: func(ctx context.Context, html string) error {
			if html != "<p>first</p>" {
				return nil
			}
			startOnce.Do(func() { close(started) })
			select {
			case <-release:
				return nil
			case <-ctx.Done():
				return ctx.Err()
			}
		},
		measureFn: func(context.Context) (float64, error) {
			return 321, nil
		},
	}
	m := testManager(t, cfg, driver)
	waitHealth(t, m, true)

	firstResult := make(chan error, 1)
	go func() {
		_, err := m.Measure(context.Background(), "<p>first</p>")
		firstResult <- err
	}()
	select {
	case <-started:
	case <-time.After(3 * time.Second):
		t.Fatal("first render did not start")
	}

	queuedCtx, cancelQueued := context.WithCancel(context.Background())
	queuedResult := make(chan error, 1)
	go func() {
		_, err := m.Measure(queuedCtx, "<p>queued</p>")
		queuedResult <- err
	}()
	waitQueueLength(t, m, 1)

	if _, err := m.Measure(context.Background(), "<p>overflow</p>"); !errors.Is(err, ErrQueueFull) {
		t.Fatalf("overflow error = %v, want ErrQueueFull", err)
	}

	cancelQueued()
	waitQueueLength(t, m, 0)
	if err := waitResult(t, queuedResult); !errors.Is(err, context.Canceled) {
		t.Fatalf("queued result = %v, want context.Canceled", err)
	}

	replacementResult := make(chan error, 1)
	go func() {
		_, err := m.Measure(context.Background(), "<p>replacement</p>")
		replacementResult <- err
	}()
	waitQueueLength(t, m, 1)
	close(release)

	if err := waitResult(t, firstResult); err != nil {
		t.Fatalf("first render failed: %v", err)
	}
	if err := waitResult(t, replacementResult); err != nil {
		t.Fatalf("replacement render failed: %v", err)
	}
}

func TestManagerRecyclesRenderTabs(t *testing.T) {
	cfg := testConfig()
	cfg.BrowserRecycleAfter = 2
	driver := &fakeDriver{
		measureFn: func(context.Context) (float64, error) {
			return 900, nil
		},
	}
	m := testManager(t, cfg, driver)
	waitHealth(t, m, true)

	for i := 0; i < 3; i++ {
		height, err := m.Measure(context.Background(), "<p>job</p>")
		if err != nil {
			t.Fatalf("measure %d failed: %v", i, err)
		}
		if height != 900 {
			t.Fatalf("measure %d height = %v, want 900", i, height)
		}
	}

	if got := driver.tabCalls(); got < 3 {
		t.Fatalf("tab creation count = %d, want at least 3 (health + render + recycle)", got)
	}
}

func TestManagerRecoversAfterBrowserProbeFailure(t *testing.T) {
	cfg := testConfig()
	cfg.BrowserHealthInterval = 10 * time.Millisecond
	driver := &fakeDriver{}
	m := testManager(t, cfg, driver)
	waitHealth(t, m, true)
	initialTabs := driver.tabCalls()

	driver.setProbeError(errors.New("browser process exited"))
	waitHealth(t, m, false)

	ctx, cancel := context.WithTimeout(context.Background(), 100*time.Millisecond)
	defer cancel()
	if _, err := m.Measure(ctx, "<p>unavailable</p>"); !errors.Is(err, ErrUnavailable) {
		t.Fatalf("measure while down = %v, want ErrUnavailable", err)
	}

	driver.setProbeError(nil)
	waitHealth(t, m, true)
	if got := driver.tabCalls(); got <= initialTabs {
		t.Fatalf("tab creation count = %d, want a restarted generation above %d", got, initialTabs)
	}
}

func TestManagerBeginShutdownRejectsNewRequests(t *testing.T) {
	m := testManager(t, testConfig(), &fakeDriver{})
	waitHealth(t, m, true)

	m.BeginShutdown()
	if health := m.Health(); health.Ready {
		t.Fatalf("health = %+v, want not ready", health)
	}
	if _, err := m.Measure(context.Background(), "<p>late</p>"); !errors.Is(err, ErrUnavailable) {
		t.Fatalf("measure after shutdown = %v, want ErrUnavailable", err)
	}
}

func TestNextBackoff(t *testing.T) {
	if got := nextBackoff(time.Second); got != 2*time.Second {
		t.Fatalf("nextBackoff(1s) = %s, want 2s", got)
	}
	if got := nextBackoff(20 * time.Second); got != 30*time.Second {
		t.Fatalf("nextBackoff(20s) = %s, want 30s", got)
	}
	if got := nextBackoff(30 * time.Second); got != 30*time.Second {
		t.Fatalf("nextBackoff(30s) = %s, want 30s", got)
	}
}
