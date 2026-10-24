package render

import (
	"context"
	"fmt"
	"time"

	"github.com/129duckflew/resume-builder/pdf-service/internal/config"
	"github.com/chromedp/cdproto/cdp"
	"github.com/chromedp/cdproto/network"
	"github.com/chromedp/cdproto/page"
	"github.com/chromedp/chromedp"
)

const (
	a4WidthPixels  = 794
	a4HeightPixels = 1123

	footerTemplate = `<div style="font-size:8px; width:100%; text-align:center; color:#666666;
font-family:Helvetica, Arial, sans-serif;">
第 <span class="pageNumber"></span> 页 / 共 <span class="totalPages"></span> 页
</div>`
)

// These aliases keep the browser implementation isolated from the manager.
type chromedpExecAllocatorOption = chromedp.ExecAllocatorOption

type renderTab struct {
	ctx    context.Context
	cancel context.CancelFunc
}

type cdpDriver struct{}

func (cdpDriver) Launch(ctx context.Context) error {
	_, err := runBrowserProbe(ctx)
	return err
}

func (cdpDriver) Probe(ctx context.Context) error {
	_, err := runBrowserProbe(ctx)
	return err
}

func (cdpDriver) NewTab(ctx context.Context, cfg config.Config) (*renderTab, error) {
	return newRenderTab(ctx, cfg)
}

func (cdpDriver) Load(ctx context.Context, html string) error {
	return loadDocument(ctx, html)
}

func (cdpDriver) Measure(ctx context.Context) (float64, error) {
	return measureDocument(ctx)
}

func (cdpDriver) PrintPDF(ctx context.Context) ([]byte, error) {
	return printDocument(ctx)
}

func defaultBrowserOptions(cfg config.Config) []chromedp.ExecAllocatorOption {
	options := append([]chromedp.ExecAllocatorOption{}, chromedp.DefaultExecAllocatorOptions[:]...)
	options = append(options,
		chromedp.Flag("no-sandbox", true),
		chromedp.Flag("disable-dev-shm-usage", true),
		chromedp.Flag("hide-scrollbars", true),
		chromedp.Flag("mute-audio", true),
		chromedp.Flag("use-gl", "angle"),
		chromedp.Flag("use-angle", "swiftshader"),
		chromedp.Flag("enable-unsafe-swiftshader", true),
	)
	if cfg.ChromiumPath != "" {
		options = append(options, chromedp.ExecPath(cfg.ChromiumPath))
	}
	return options
}

func newBrowserAllocator(ctx context.Context, options ...chromedp.ExecAllocatorOption) (context.Context, context.CancelFunc) {
	return chromedp.NewExecAllocator(ctx, options...)
}

func newBrowserContext(ctx context.Context) (context.Context, context.CancelFunc) {
	return chromedp.NewContext(ctx)
}

func runBrowserProbe(ctx context.Context) (int, error) {
	return chromedp.Run(ctx, chromedp.Evaluate[int](`1`))
}

func newRenderTab(browserCtx context.Context, cfg config.Config) (*renderTab, error) {
	tabCtx, cancel := chromedp.NewContext(browserCtx)
	done := make(chan error, 1)
	go func() {
		actions := []chromedp.Action[chromedp.Void]{
			chromedp.EmulateViewport(a4WidthPixels, a4HeightPixels, chromedp.EmulatePortrait),
		}
		if cfg.BlockExternalResources {
			actions = append(actions, blockExternalResources())
		}
		actions = append(actions, chromedp.Navigate("about:blank"))
		done <- chromedp.Do(tabCtx, actions...)
	}()

	select {
	case err := <-done:
		if err != nil {
			cancel()
			return nil, err
		}
		return &renderTab{ctx: tabCtx, cancel: cancel}, nil
	case <-time.After(cfg.BrowserStartupTimeout):
		cancel()
		return nil, fmt.Errorf("tab startup exceeded %s", cfg.BrowserStartupTimeout)
	case <-browserCtx.Done():
		cancel()
		return nil, browserCtx.Err()
	}
}

func blockExternalResources() chromedp.Action[chromedp.Void] {
	return chromedp.Func(func(ctx context.Context, target *chromedp.Target) error {
		if _, err := cdp.Call(ctx, target, network.Enable, network.EnableParams{}); err != nil {
			return err
		}
		_, err := cdp.Call(ctx, target, network.SetBlockedURLs, network.SetBlockedURLsParams{
			URLPatterns: []*network.BlockPattern{
				{URLPattern: "http://*", Block: true},
				{URLPattern: "https://*", Block: true},
				{URLPattern: "ws://*", Block: true},
				{URLPattern: "wss://*", Block: true},
			},
		})
		return err
	})
}

func loadDocument(ctx context.Context, html string) error {
	if err := chromedp.Do(ctx, chromedp.Navigate("about:blank")); err != nil {
		return err
	}

	tree, err := chromedp.Call(ctx, page.GetFrameTree, cdp.Empty{})
	if err != nil {
		return err
	}
	_, err = chromedp.Call(ctx, page.SetDocumentContent, page.SetDocumentContentParams{
		FrameID: tree.FrameTree.Frame.ID,
		HTML:    html,
	})
	if err != nil {
		return err
	}

	_, err = chromedp.Run(ctx, chromedp.Poll[chromedp.Void](
		`document.readyState === "complete" && document.fonts.status === "loaded"`,
		chromedp.WithPollingTimeout(5*time.Second),
		chromedp.WithPollingInterval(10*time.Millisecond),
	))
	return err
}

func measureDocument(ctx context.Context) (float64, error) {
	height, err := chromedp.Run(ctx, chromedp.Evaluate[float64](
		`document.body ? document.body.scrollHeight : 0`,
	))
	if err != nil {
		return 0, fmt.Errorf("measure scroll height: %w", err)
	}
	return height, nil
}

func printDocument(ctx context.Context) ([]byte, error) {
	pdf, err := chromedp.Run(ctx, chromedp.PrintToPDF(
		chromedp.PDFPaper(chromedp.PaperA4),
		chromedp.PDFMargins(0, 0, 0, 0),
		chromedp.PDFPreferCSSPageSize(),
		chromedp.PDFPrintBackground(),
		chromedp.PDFHeaderTemplate(`<div></div>`),
		chromedp.PDFFooterTemplate(footerTemplate),
		chromedp.PDFStream(),
	))
	if err != nil {
		return nil, fmt.Errorf("print PDF: %w", err)
	}
	if len(pdf) == 0 {
		return nil, fmt.Errorf("print PDF returned no data")
	}
	return pdf, nil
}

func (g *generation) doneClosed() bool {
	select {
	case <-g.done:
		return true
	default:
		return false
	}
}
