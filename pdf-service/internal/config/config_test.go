package config

import (
	"testing"
	"time"
)

func TestLoadFromDefaults(t *testing.T) {
	cfg, err := LoadFrom(func(string) (string, bool) { return "", false })
	if err != nil {
		t.Fatalf("LoadFrom returned error: %v", err)
	}

	if cfg.Port != "8090" {
		t.Fatalf("Port = %q, want 8090", cfg.Port)
	}
	if cfg.MaxConcurrency != 1 {
		t.Fatalf("MaxConcurrency = %d, want 1", cfg.MaxConcurrency)
	}
	if cfg.QueueCapacity != 8 {
		t.Fatalf("QueueCapacity = %d, want 8", cfg.QueueCapacity)
	}
	if cfg.RequestTimeout != 30*time.Second {
		t.Fatalf("RequestTimeout = %s, want 30s", cfg.RequestTimeout)
	}
	if !cfg.BlockExternalResources {
		t.Fatal("BlockExternalResources = false, want true")
	}
}

func TestLoadFromOverrides(t *testing.T) {
	values := map[string]string{
		"PORT":                     "9000",
		"MAX_CONCURRENCY":          "2",
		"QUEUE_CAPACITY":           "3",
		"REQUEST_TIMEOUT":          "12s",
		"BROWSER_STARTUP_TIMEOUT":  "8s",
		"SHUTDOWN_TIMEOUT":         "4s",
		"BROWSER_HEALTH_INTERVAL":  "2s",
		"MAX_BODY_BYTES":           "1024",
		"CHROMIUM_PATH":            "/usr/bin/headless_shell",
		"BROWSER_RECYCLE_AFTER":    "42",
		"BLOCK_EXTERNAL_RESOURCES": "false",
	}

	cfg, err := LoadFrom(func(key string) (string, bool) {
		value, ok := values[key]
		return value, ok
	})
	if err != nil {
		t.Fatalf("LoadFrom returned error: %v", err)
	}

	if cfg.Port != "9000" || cfg.MaxConcurrency != 2 || cfg.QueueCapacity != 3 {
		t.Fatalf("unexpected scalar config: %+v", cfg)
	}
	if cfg.RequestTimeout != 12*time.Second || cfg.BrowserStartupTimeout != 8*time.Second {
		t.Fatalf("unexpected timeout config: %+v", cfg)
	}
	if cfg.ChromiumPath != "/usr/bin/headless_shell" || cfg.BrowserRecycleAfter != 42 {
		t.Fatalf("unexpected browser config: %+v", cfg)
	}
	if cfg.BlockExternalResources {
		t.Fatalf("unexpected boolean config: %+v", cfg)
	}
}

func TestLoadFromRejectsInvalidConcurrency(t *testing.T) {
	_, err := LoadFrom(func(key string) (string, bool) {
		if key == "MAX_CONCURRENCY" {
			return "3", true
		}
		return "", false
	})
	if err == nil {
		t.Fatal("LoadFrom accepted MAX_CONCURRENCY=3")
	}
}

func TestLoadFromRejectsInvalidDuration(t *testing.T) {
	_, err := LoadFrom(func(key string) (string, bool) {
		if key == "REQUEST_TIMEOUT" {
			return "soon", true
		}
		return "", false
	})
	if err == nil {
		t.Fatal("LoadFrom accepted an invalid duration")
	}
}
