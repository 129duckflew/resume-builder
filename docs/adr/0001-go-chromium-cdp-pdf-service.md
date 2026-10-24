# ADR 0001: Run the PDF Service in Go with a Pinned Chromium

- Status: Accepted
- Date: 2026-10-25

## Context

The previous PDF Service embedded a JVM, Spring, a Playwright Node driver, and
Chromium. It created a browser context and page for every request. For a
three-endpoint internal renderer this produced a large image, high idle memory,
and unbounded request amplification.

HTML/CSS rendering fidelity is a hard requirement. Replacing Chromium with a
native PDF library would change theme output and is out of scope.

## Decision

- Implement the service in Go and speak CDP directly with `chromedp`.
- Keep one Chromium process and reuse one tab per render worker.
- Bound the queue and default to one worker; include queue wait in the request
  deadline and remove cancelled jobs from the queue immediately.
- Keep `/health`, `/pdf`, `/measure`, and the Kubernetes health aliases
  contract-compatible with the backend.
- Pin Playwright Chromium build `1228` (Chromium 149). Download it directly
  from the Playwright CDN and verify SHA-256 during the Docker build.
- Build a minimal Ubuntu 24.04 runtime with only the required libraries and
  fonts. Run as non-root with a read-only root filesystem and a writable
  `/tmp` tmpfs.
- Target `linux/arm64`, which is the deployment architecture.

Rust was considered. Its expected saving outside Chromium is only about
5-15 MiB and does not justify the additional async CDP lifecycle and delivery
risk for this service.

## Consequences

- The JVM, Spring, and Playwright Node driver are removed from the runtime.
- Chromium remains the dominant memory consumer, so per-pod concurrency stays
  bounded and scaling should prefer additional replicas.
- Chromium upgrades are deliberate: a new build requires updating the CDN URL
  and checksum, then validating PDF pagination and measurement.
- The image currently targets ARM64 only. Adding AMD64 requires a pinned AMD64
  artifact and checksum before the Dockerfile can accept that target.
- External HTTP(S) and WebSocket resources are blocked by default. Resumes must
  use inline content or `data:` assets.
