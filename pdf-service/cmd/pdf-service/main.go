package main

import (
	"context"
	"errors"
	"fmt"
	"log/slog"
	"net/http"
	"os"
	"os/signal"
	"strings"
	"syscall"
	"time"

	"github.com/129duckflew/resume-builder/pdf-service/internal/config"
	"github.com/129duckflew/resume-builder/pdf-service/internal/httpapi"
	"github.com/129duckflew/resume-builder/pdf-service/internal/render"
)

func main() {
	if err := prepareRuntimeDirs(); err != nil {
		slog.Error("prepare runtime directories", "error", err)
		os.Exit(1)
	}

	cfg, err := config.LoadFrom(os.LookupEnv)
	if err != nil {
		slog.Error("invalid configuration", "error", err)
		os.Exit(1)
	}

	logger := newLogger()
	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	// Keep the browser independent from the signal context. HTTP shutdown must
	// drain accepted requests before the renderer is closed.
	renderer := render.NewManager(context.Background(), cfg, logger)
	defer renderer.Close()

	api := httpapi.New(cfg, renderer, logger)
	server := &http.Server{
		Addr:              ":" + cfg.Port,
		Handler:           api.Handler(),
		ReadHeaderTimeout: 5 * time.Second,
		ReadTimeout:       cfg.RequestTimeout + 10*time.Second,
		WriteTimeout:      cfg.RequestTimeout + 10*time.Second,
		IdleTimeout:       60 * time.Second,
	}

	serverErr := make(chan error, 1)
	go func() {
		logger.Info("pdf service listening", "address", server.Addr)
		serverErr <- server.ListenAndServe()
	}()

	select {
	case <-ctx.Done():
		logger.Info("shutdown signal received")
	case err := <-serverErr:
		if !errors.Is(err, http.ErrServerClosed) {
			logger.Error("http server stopped", "error", err)
			stop()
		}
	}

	renderer.BeginShutdown()
	shutdownCtx, cancel := context.WithTimeout(context.Background(), cfg.ShutdownTimeout)
	defer cancel()
	if err := server.Shutdown(shutdownCtx); err != nil {
		logger.Warn("http shutdown failed", "error", err)
	}
	if err := renderer.Close(); err != nil {
		logger.Warn("renderer shutdown failed", "error", err)
	}
}

func prepareRuntimeDirs() error {
	for _, key := range []string{"HOME", "TMPDIR"} {
		path := os.Getenv(key)
		if path == "" || path == "/" {
			continue
		}
		if err := os.MkdirAll(path, 0o700); err != nil {
			return fmt.Errorf("create %s directory %q: %w", key, path, err)
		}
	}
	return nil
}

func newLogger() *slog.Logger {
	level := slog.LevelInfo
	switch strings.ToLower(os.Getenv("LOG_LEVEL")) {
	case "debug":
		level = slog.LevelDebug
	case "warn", "warning":
		level = slog.LevelWarn
	case "error":
		level = slog.LevelError
	}
	handler := slog.NewJSONHandler(os.Stdout, &slog.HandlerOptions{Level: level})
	return slog.New(handler)
}
