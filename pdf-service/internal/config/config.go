package config

import (
	"fmt"
	"strconv"
	"time"
)

const (
	defaultPort                  = "8090"
	defaultMaxConcurrency        = 1
	defaultQueueCapacity         = 8
	defaultRequestTimeout        = 30 * time.Second
	defaultBrowserStartupTimeout = 30 * time.Second
	defaultShutdownTimeout       = 15 * time.Second
	defaultMaxBodyBytes          = int64(4 << 20)
	defaultBrowserRecycleAfter   = 100
	defaultBrowserHealthInterval = 5 * time.Second
)

// Config contains the runtime settings for the PDF service.
type Config struct {
	Port                   string
	MaxConcurrency         int
	QueueCapacity          int
	RequestTimeout         time.Duration
	BrowserStartupTimeout  time.Duration
	ShutdownTimeout        time.Duration
	BrowserHealthInterval  time.Duration
	MaxBodyBytes           int64
	ChromiumPath           string
	BrowserRecycleAfter    int
	BlockExternalResources bool
}

// Default returns the documented defaults.
func Default() Config {
	return Config{
		Port:                   defaultPort,
		MaxConcurrency:         defaultMaxConcurrency,
		QueueCapacity:          defaultQueueCapacity,
		RequestTimeout:         defaultRequestTimeout,
		BrowserStartupTimeout:  defaultBrowserStartupTimeout,
		ShutdownTimeout:        defaultShutdownTimeout,
		BrowserHealthInterval:  defaultBrowserHealthInterval,
		MaxBodyBytes:           defaultMaxBodyBytes,
		BrowserRecycleAfter:    defaultBrowserRecycleAfter,
		BlockExternalResources: true,
	}
}

// LoadFrom reads configuration from the provided environment lookup.
func LoadFrom(lookup func(string) (string, bool)) (Config, error) {
	cfg := Default()
	var err error

	if value, ok := lookup("PORT"); ok {
		cfg.Port = value
	}
	if cfg.Port == "" {
		return Config{}, fmt.Errorf("PORT must not be empty")
	}

	cfg.MaxConcurrency, err = envInt(lookup, "MAX_CONCURRENCY", cfg.MaxConcurrency)
	if err != nil {
		return Config{}, err
	}
	if cfg.MaxConcurrency < 1 || cfg.MaxConcurrency > 2 {
		return Config{}, fmt.Errorf("MAX_CONCURRENCY must be between 1 and 2")
	}

	cfg.QueueCapacity, err = envInt(lookup, "QUEUE_CAPACITY", cfg.QueueCapacity)
	if err != nil {
		return Config{}, err
	}
	if cfg.QueueCapacity < 0 {
		return Config{}, fmt.Errorf("QUEUE_CAPACITY must not be negative")
	}

	cfg.RequestTimeout, err = envDuration(lookup, "REQUEST_TIMEOUT", cfg.RequestTimeout)
	if err != nil {
		return Config{}, err
	}
	if cfg.RequestTimeout <= 0 {
		return Config{}, fmt.Errorf("REQUEST_TIMEOUT must be positive")
	}

	cfg.BrowserStartupTimeout, err = envDuration(lookup, "BROWSER_STARTUP_TIMEOUT", cfg.BrowserStartupTimeout)
	if err != nil {
		return Config{}, err
	}
	if cfg.BrowserStartupTimeout <= 0 {
		return Config{}, fmt.Errorf("BROWSER_STARTUP_TIMEOUT must be positive")
	}

	cfg.ShutdownTimeout, err = envDuration(lookup, "SHUTDOWN_TIMEOUT", cfg.ShutdownTimeout)
	if err != nil {
		return Config{}, err
	}
	if cfg.ShutdownTimeout <= 0 {
		return Config{}, fmt.Errorf("SHUTDOWN_TIMEOUT must be positive")
	}

	cfg.BrowserHealthInterval, err = envDuration(lookup, "BROWSER_HEALTH_INTERVAL", cfg.BrowserHealthInterval)
	if err != nil {
		return Config{}, err
	}
	if cfg.BrowserHealthInterval <= 0 {
		return Config{}, fmt.Errorf("BROWSER_HEALTH_INTERVAL must be positive")
	}

	cfg.MaxBodyBytes, err = envInt64(lookup, "MAX_BODY_BYTES", cfg.MaxBodyBytes)
	if err != nil {
		return Config{}, err
	}
	if cfg.MaxBodyBytes <= 0 {
		return Config{}, fmt.Errorf("MAX_BODY_BYTES must be positive")
	}

	if value, ok := lookup("CHROMIUM_PATH"); ok {
		cfg.ChromiumPath = value
	}

	cfg.BrowserRecycleAfter, err = envInt(lookup, "BROWSER_RECYCLE_AFTER", cfg.BrowserRecycleAfter)
	if err != nil {
		return Config{}, err
	}
	if cfg.BrowserRecycleAfter < 1 {
		return Config{}, fmt.Errorf("BROWSER_RECYCLE_AFTER must be at least 1")
	}

	cfg.BlockExternalResources, err = envBool(lookup, "BLOCK_EXTERNAL_RESOURCES", cfg.BlockExternalResources)
	if err != nil {
		return Config{}, err
	}

	return cfg, nil
}

func envInt(lookup func(string) (string, bool), key string, fallback int) (int, error) {
	value, ok := lookup(key)
	if !ok || value == "" {
		return fallback, nil
	}
	parsed, err := strconv.Atoi(value)
	if err != nil {
		return 0, fmt.Errorf("%s must be an integer: %w", key, err)
	}
	return parsed, nil
}

func envInt64(lookup func(string) (string, bool), key string, fallback int64) (int64, error) {
	value, ok := lookup(key)
	if !ok || value == "" {
		return fallback, nil
	}
	parsed, err := strconv.ParseInt(value, 10, 64)
	if err != nil {
		return 0, fmt.Errorf("%s must be an integer: %w", key, err)
	}
	return parsed, nil
}

func envDuration(lookup func(string) (string, bool), key string, fallback time.Duration) (time.Duration, error) {
	value, ok := lookup(key)
	if !ok || value == "" {
		return fallback, nil
	}
	parsed, err := time.ParseDuration(value)
	if err != nil {
		return 0, fmt.Errorf("%s must be a duration: %w", key, err)
	}
	return parsed, nil
}

func envBool(lookup func(string) (string, bool), key string, fallback bool) (bool, error) {
	value, ok := lookup(key)
	if !ok || value == "" {
		return fallback, nil
	}
	parsed, err := strconv.ParseBool(value)
	if err != nil {
		return false, fmt.Errorf("%s must be a boolean: %w", key, err)
	}
	return parsed, nil
}
