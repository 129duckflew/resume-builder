package httpapi

import (
	"context"
	"errors"
	"io"
	"log/slog"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"

	"github.com/129duckflew/resume-builder/pdf-service/internal/config"
	"github.com/129duckflew/resume-builder/pdf-service/internal/render"
)

type fakeRenderer struct {
	health    render.Health
	measureFn func(context.Context, string) (float64, error)
	pdfFn     func(context.Context, string) ([]byte, error)
}

func (f *fakeRenderer) Measure(ctx context.Context, html string) (float64, error) {
	if f.measureFn == nil {
		return 0, nil
	}
	return f.measureFn(ctx, html)
}

func (f *fakeRenderer) PDF(ctx context.Context, html string) ([]byte, error) {
	if f.pdfFn == nil {
		return nil, nil
	}
	return f.pdfFn(ctx, html)
}

func (f *fakeRenderer) Health() render.Health {
	return f.health
}

func testServer(t *testing.T, renderer render.Renderer) *Server {
	t.Helper()
	cfg := config.Default()
	cfg.MaxBodyBytes = 64
	logger := slog.New(slog.NewTextHandler(io.Discard, nil))
	return New(cfg, renderer, logger)
}

func TestHealthReflectsReadiness(t *testing.T) {
	t.Run("ready", func(t *testing.T) {
		server := testServer(t, &fakeRenderer{health: render.Health{Ready: true}})
		recorder := httptest.NewRecorder()
		server.Handler().ServeHTTP(recorder, httptest.NewRequest(http.MethodGet, "/health", nil))

		if recorder.Code != http.StatusOK {
			t.Fatalf("status = %d, want 200", recorder.Code)
		}
		if !strings.Contains(recorder.Body.String(), `"status":"UP"`) {
			t.Fatalf("body = %s", recorder.Body.String())
		}
	})

	t.Run("not ready", func(t *testing.T) {
		server := testServer(t, &fakeRenderer{
			health: render.Health{Ready: false, Reason: "browser disconnected"},
		})
		recorder := httptest.NewRecorder()
		server.Handler().ServeHTTP(recorder, httptest.NewRequest(http.MethodGet, "/health", nil))

		if recorder.Code != http.StatusServiceUnavailable {
			t.Fatalf("status = %d, want 503", recorder.Code)
		}
		if recorder.Header().Get("Retry-After") != "1" {
			t.Fatalf("Retry-After = %q, want 1", recorder.Header().Get("Retry-After"))
		}
	})
}

func TestLivenessStaysUpWhenBrowserIsDown(t *testing.T) {
	server := testServer(t, &fakeRenderer{health: render.Health{Ready: false, Reason: "down"}})
	recorder := httptest.NewRecorder()
	server.Handler().ServeHTTP(recorder, httptest.NewRequest(http.MethodGet, "/actuator/health/liveness", nil))

	if recorder.Code != http.StatusOK {
		t.Fatalf("status = %d, want 200", recorder.Code)
	}
}

func TestMeasure(t *testing.T) {
	server := testServer(t, &fakeRenderer{
		health: render.Health{Ready: true},
		measureFn: func(_ context.Context, html string) (float64, error) {
			if html != "<html>hello</html>" {
				t.Fatalf("html = %q", html)
			}
			return 1123, nil
		},
	})

	request := httptest.NewRequest(http.MethodPost, "/measure", strings.NewReader("<html>hello</html>"))
	request.Header.Set("Content-Type", "text/html; charset=utf-8")
	recorder := httptest.NewRecorder()
	server.Handler().ServeHTTP(recorder, request)

	if recorder.Code != http.StatusOK {
		t.Fatalf("status = %d, body = %s", recorder.Code, recorder.Body.String())
	}
	if strings.TrimSpace(recorder.Body.String()) != `{"height":1123}` {
		t.Fatalf("body = %s", recorder.Body.String())
	}
}

func TestPDF(t *testing.T) {
	server := testServer(t, &fakeRenderer{
		health: render.Health{Ready: true},
		pdfFn: func(context.Context, string) ([]byte, error) {
			return []byte("%PDF-test"), nil
		},
	})

	request := httptest.NewRequest(http.MethodPost, "/pdf", strings.NewReader("<html></html>"))
	request.Header.Set("Content-Type", "text/html")
	recorder := httptest.NewRecorder()
	server.Handler().ServeHTTP(recorder, request)

	if recorder.Code != http.StatusOK {
		t.Fatalf("status = %d, body = %s", recorder.Code, recorder.Body.String())
	}
	if got := recorder.Header().Get("Content-Type"); got != "application/pdf" {
		t.Fatalf("Content-Type = %q", got)
	}
	if got := recorder.Header().Get("Content-Disposition"); got != `inline; filename="resume.pdf"` {
		t.Fatalf("Content-Disposition = %q", got)
	}
	if recorder.Body.String() != "%PDF-test" {
		t.Fatalf("body = %q", recorder.Body.String())
	}
}

func TestRejectsWrongContentType(t *testing.T) {
	server := testServer(t, &fakeRenderer{health: render.Health{Ready: true}})
	request := httptest.NewRequest(http.MethodPost, "/measure", strings.NewReader("{}"))
	request.Header.Set("Content-Type", "application/json")
	recorder := httptest.NewRecorder()
	server.Handler().ServeHTTP(recorder, request)

	if recorder.Code != http.StatusUnsupportedMediaType {
		t.Fatalf("status = %d, want 415", recorder.Code)
	}
}

func TestRejectsOversizedBody(t *testing.T) {
	server := testServer(t, &fakeRenderer{health: render.Health{Ready: true}})
	request := httptest.NewRequest(http.MethodPost, "/measure", strings.NewReader(strings.Repeat("x", 65)))
	request.Header.Set("Content-Type", "text/html")
	recorder := httptest.NewRecorder()
	server.Handler().ServeHTTP(recorder, request)

	if recorder.Code != http.StatusRequestEntityTooLarge {
		t.Fatalf("status = %d, want 413; body = %s", recorder.Code, recorder.Body.String())
	}
}

func TestRenderErrorMapping(t *testing.T) {
	tests := []struct {
		name       string
		err        error
		wantStatus int
	}{
		{"queue full", render.ErrQueueFull, http.StatusServiceUnavailable},
		{"unavailable", render.ErrUnavailable, http.StatusServiceUnavailable},
		{"deadline", context.DeadlineExceeded, http.StatusGatewayTimeout},
		{"canceled", context.Canceled, http.StatusRequestTimeout},
		{"failure", errors.New("boom"), http.StatusInternalServerError},
	}

	for _, test := range tests {
		t.Run(test.name, func(t *testing.T) {
			server := testServer(t, &fakeRenderer{
				health: render.Health{Ready: true},
				measureFn: func(context.Context, string) (float64, error) {
					return 0, test.err
				},
			})
			request := httptest.NewRequest(http.MethodPost, "/measure", strings.NewReader("<html></html>"))
			request.Header.Set("Content-Type", "text/html")
			recorder := httptest.NewRecorder()
			server.Handler().ServeHTTP(recorder, request)

			if recorder.Code != test.wantStatus {
				t.Fatalf("status = %d, want %d", recorder.Code, test.wantStatus)
			}
		})
	}
}

func TestMeasureHonorsRendererContext(t *testing.T) {
	server := testServer(t, &fakeRenderer{
		health: render.Health{Ready: true},
		measureFn: func(ctx context.Context, _ string) (float64, error) {
			deadline, ok := ctx.Deadline()
			if !ok {
				t.Fatal("render context has no deadline")
			}
			if time.Until(deadline) > config.Default().RequestTimeout {
				t.Fatal("render context deadline is too far in the future")
			}
			return 1123, nil
		},
	})

	request := httptest.NewRequest(http.MethodPost, "/measure", strings.NewReader("<html></html>"))
	request.Header.Set("Content-Type", "text/html")
	recorder := httptest.NewRecorder()
	server.Handler().ServeHTTP(recorder, request)
}
