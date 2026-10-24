package httpapi

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"log/slog"
	"mime"
	"net/http"
	"strconv"
	"sync/atomic"
	"time"

	"github.com/129duckflew/resume-builder/pdf-service/internal/config"
	"github.com/129duckflew/resume-builder/pdf-service/internal/render"
)

// Server exposes the PDF service HTTP contract.
type Server struct {
	cfg      config.Config
	renderer render.Renderer
	logger   *slog.Logger
	nextID   atomic.Uint64
}

// New creates an HTTP server around a renderer.
func New(cfg config.Config, renderer render.Renderer, logger *slog.Logger) *Server {
	if logger == nil {
		logger = slog.Default()
	}
	return &Server{cfg: cfg, renderer: renderer, logger: logger}
}

// Handler returns the complete HTTP handler.
func (s *Server) Handler() http.Handler {
	mux := http.NewServeMux()
	mux.HandleFunc("GET /health", s.handleHealth)
	mux.HandleFunc("GET /actuator/health", s.handleActuatorHealth)
	mux.HandleFunc("GET /actuator/health/liveness", s.handleLiveness)
	mux.HandleFunc("GET /actuator/health/readiness", s.handleReadiness)
	mux.HandleFunc("POST /measure", s.handleMeasure)
	mux.HandleFunc("POST /pdf", s.handlePDF)
	return s.recover(s.logRequests(mux))
}

func (s *Server) handleHealth(w http.ResponseWriter, _ *http.Request) {
	health := s.renderer.Health()
	if !health.Ready {
		w.Header().Set("Retry-After", "1")
		writeJSON(w, http.StatusServiceUnavailable, map[string]string{
			"status": "DOWN",
			"reason": health.Reason,
		})
		return
	}
	writeJSON(w, http.StatusOK, map[string]string{"status": "UP"})
}

func (s *Server) handleActuatorHealth(w http.ResponseWriter, _ *http.Request) {
	health := s.renderer.Health()
	status := http.StatusOK
	body := map[string]any{
		"status": "UP",
		"groups": []string{"liveness", "readiness"},
	}
	if !health.Ready {
		status = http.StatusServiceUnavailable
		body["status"] = "DOWN"
		body["reason"] = health.Reason
	}
	writeJSON(w, status, body)
}

func (s *Server) handleLiveness(w http.ResponseWriter, _ *http.Request) {
	writeJSON(w, http.StatusOK, map[string]string{"status": "UP"})
}

func (s *Server) handleReadiness(w http.ResponseWriter, _ *http.Request) {
	health := s.renderer.Health()
	if !health.Ready {
		w.Header().Set("Retry-After", "1")
		writeJSON(w, http.StatusServiceUnavailable, map[string]string{
			"status": "DOWN",
			"reason": health.Reason,
		})
		return
	}
	writeJSON(w, http.StatusOK, map[string]string{"status": "UP"})
}

func (s *Server) handleMeasure(w http.ResponseWriter, r *http.Request) {
	body, ok := s.readHTMLBody(w, r)
	if !ok {
		return
	}

	ctx, cancel := context.WithTimeout(r.Context(), s.cfg.RequestTimeout)
	defer cancel()

	height, err := s.renderer.Measure(ctx, string(body))
	if err != nil {
		s.writeRenderError(w, err)
		return
	}
	writeJSON(w, http.StatusOK, map[string]float64{"height": height})
}

func (s *Server) handlePDF(w http.ResponseWriter, r *http.Request) {
	body, ok := s.readHTMLBody(w, r)
	if !ok {
		return
	}

	ctx, cancel := context.WithTimeout(r.Context(), s.cfg.RequestTimeout)
	defer cancel()

	pdf, err := s.renderer.PDF(ctx, string(body))
	if err != nil {
		s.writeRenderError(w, err)
		return
	}

	w.Header().Set("Content-Disposition", `inline; filename="resume.pdf"`)
	w.Header().Set("Content-Type", "application/pdf")
	w.Header().Set("Content-Length", strconv.Itoa(len(pdf)))
	w.WriteHeader(http.StatusOK)
	if _, err := w.Write(pdf); err != nil {
		s.logger.Warn("write PDF response failed", "error", err)
	}
}

func (s *Server) readHTMLBody(w http.ResponseWriter, r *http.Request) ([]byte, bool) {
	mediaType, _, err := mime.ParseMediaType(r.Header.Get("Content-Type"))
	if err != nil || mediaType != "text/html" {
		writeError(w, http.StatusUnsupportedMediaType, "unsupported_media_type", "Content-Type must be text/html")
		return nil, false
	}

	defer r.Body.Close()
	reader := http.MaxBytesReader(w, r.Body, s.cfg.MaxBodyBytes)
	body, err := io.ReadAll(reader)
	if err != nil {
		var maxErr *http.MaxBytesError
		if errors.As(err, &maxErr) {
			writeError(w, http.StatusRequestEntityTooLarge, "body_too_large", "request body exceeds configured limit")
			return nil, false
		}
		writeError(w, http.StatusBadRequest, "invalid_body", "failed to read request body")
		return nil, false
	}
	return body, true
}

func (s *Server) writeRenderError(w http.ResponseWriter, err error) {
	switch {
	case errors.Is(err, render.ErrQueueFull):
		w.Header().Set("Retry-After", "1")
		writeError(w, http.StatusServiceUnavailable, "queue_full", "render queue is full")
	case errors.Is(err, render.ErrUnavailable):
		w.Header().Set("Retry-After", "1")
		writeError(w, http.StatusServiceUnavailable, "unavailable", "renderer is unavailable")
	case errors.Is(err, context.DeadlineExceeded):
		writeError(w, http.StatusGatewayTimeout, "timeout", "render timed out")
	case errors.Is(err, context.Canceled):
		writeError(w, http.StatusRequestTimeout, "request_canceled", "request was canceled")
	default:
		s.logger.Error("render failed", "error", err)
		writeError(w, http.StatusInternalServerError, "render_failed", "render failed")
	}
}

func (s *Server) recover(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		defer func() {
			if recovered := recover(); recovered != nil {
				s.logger.Error("panic handling request", "panic", recovered)
				writeError(w, http.StatusInternalServerError, "internal_error", "internal server error")
			}
		}()
		next.ServeHTTP(w, r)
	})
}

func (s *Server) logRequests(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		started := time.Now()
		requestID := "req-" + strconv.FormatUint(s.nextID.Add(1), 10)
		w.Header().Set("X-Request-ID", requestID)
		recorder := &responseRecorder{ResponseWriter: w, status: http.StatusOK}

		next.ServeHTTP(recorder, r)

		level := slog.LevelInfo
		if recorder.status >= http.StatusInternalServerError {
			level = slog.LevelError
		} else if recorder.status >= http.StatusBadRequest {
			level = slog.LevelWarn
		}
		s.logger.Log(r.Context(), level, "http request",
			"request_id", requestID,
			"method", r.Method,
			"path", r.URL.Path,
			"status", recorder.status,
			"bytes", recorder.bytes,
			"duration_ms", time.Since(started).Milliseconds(),
		)
	})
}

type responseRecorder struct {
	http.ResponseWriter
	status int
	bytes  int
}

func (r *responseRecorder) WriteHeader(status int) {
	r.status = status
	r.ResponseWriter.WriteHeader(status)
}

func (r *responseRecorder) Write(data []byte) (int, error) {
	written, err := r.ResponseWriter.Write(data)
	r.bytes += written
	return written, err
}

func writeError(w http.ResponseWriter, status int, code, message string) {
	writeJSON(w, status, map[string]string{
		"code":  code,
		"error": message,
	})
}

func writeJSON(w http.ResponseWriter, status int, value any) {
	data, err := json.Marshal(value)
	if err != nil {
		http.Error(w, fmt.Sprintf("encode response: %v", err), http.StatusInternalServerError)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	w.Header().Set("Content-Length", strconv.Itoa(len(data)+1))
	w.WriteHeader(status)
	_, _ = w.Write(append(data, '\n'))
}
