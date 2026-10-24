FROM golang:1.26-bookworm@sha256:632dff7351799a5a9954a48dc06670ade72ee44e8c351cdc0e5a57af2fea21af AS build
WORKDIR /src

COPY go.mod go.sum ./
RUN go mod download

COPY . .
ARG TARGETOS=linux
ARG TARGETARCH
RUN CGO_ENABLED=0 GOOS=${TARGETOS} GOARCH=${TARGETARCH} go build \
    -trimpath \
    -ldflags="-s -w" \
    -o /out/pdf-service \
    ./cmd/pdf-service

# Chromium 149 (Playwright build 1228) is the exact engine used by Playwright
# 1.61.0. Using a newer Chromium changes font metrics and can break the
# +/-1px measurement contract.
FROM golang:1.26-bookworm@sha256:632dff7351799a5a9954a48dc06670ade72ee44e8c351cdc0e5a57af2fea21af AS chromium
ARG TARGETARCH
ARG CHROMIUM_URL=https://cdn.playwright.dev/dbazure/download/playwright/builds/chromium/1228/chromium-headless-shell-linux-arm64.zip
ARG CHROMIUM_SHA256=1652929a70f4afb17aca36fce073fb7ed22262d16825be761b0801972f43ac4f
RUN test "${TARGETARCH}" = "arm64" \
    && curl -fL --retry 3 --retry-all-errors "${CHROMIUM_URL}" -o /tmp/chromium.zip \
    && echo "${CHROMIUM_SHA256}  /tmp/chromium.zip" | sha256sum -c - \
    && mkdir -p /opt/chromium \
    && python3 -c 'import zipfile; zipfile.ZipFile("/tmp/chromium.zip").extractall("/opt/chromium")' \
    && chmod 0755 /opt/chromium/chrome-linux/headless_shell \
    && rm -f /tmp/chromium.zip

FROM ubuntu:24.04@sha256:68ee22004a9109b44973313b02ff7b1de250f1de0f5ecb628d73f579aaff33ac

RUN apt-get -o Acquire::Retries=3 -o Acquire::http::Timeout=20 update \
    && DEBIAN_FRONTEND=noninteractive apt-get -o Acquire::Retries=3 -o Acquire::http::Timeout=20 install -y --no-install-recommends \
        ca-certificates \
        fontconfig \
        fonts-dejavu-core \
        fonts-liberation \
        fonts-noto-color-emoji \
        fonts-wqy-zenhei \
        libasound2t64 \
        libatk-bridge2.0-0t64 \
        libatk1.0-0t64 \
        libatspi2.0-0t64 \
        libcairo2 \
        libcups2t64 \
        libdbus-1-3 \
        libdrm2 \
        libglib2.0-0t64 \
        libnspr4 \
        libnss3 \
        libpango-1.0-0 \
        libx11-6 \
        libxcb1 \
        libxcomposite1 \
        libxdamage1 \
        libxext6 \
        libxfixes3 \
        libxkbcommon0 \
        libxrandr2 \
        wget \
    && cd /tmp \
    && apt-get download libgbm1 \
    && mkdir -p /tmp/gbm \
    && dpkg-deb -x /tmp/libgbm1_*.deb /tmp/gbm \
    && cp -a /tmp/gbm/usr/lib/aarch64-linux-gnu/libgbm.so.1* /usr/lib/aarch64-linux-gnu/ \
    && rm -rf /tmp/gbm /tmp/libgbm1_*.deb \
    && rm -rf /var/lib/apt/lists/* /usr/share/doc/* /usr/share/man/*

RUN fc-cache -f

COPY --from=chromium /opt/chromium /opt/chromium
COPY --from=build /out/pdf-service /usr/local/bin/pdf-service

RUN groupadd --gid 10001 pdf \
    && useradd --uid 10001 --gid pdf --no-create-home --shell /usr/sbin/nologin pdf \
    && mkdir -p /tmp/pdf-service \
    && chown -R pdf:pdf /tmp/pdf-service

ENV BLOCK_EXTERNAL_RESOURCES=true \
    CHROMIUM_PATH=/opt/chromium/chrome-linux/headless_shell \
    HOME=/tmp/pdf-service \
    PORT=8090 \
    TMPDIR=/tmp/pdf-service

USER 10001:10001
EXPOSE 8090

HEALTHCHECK --interval=5s --timeout=3s --start-period=10s --retries=12 \
    CMD wget -qO- http://127.0.0.1:8090/health >/dev/null || exit 1

ENTRYPOINT ["/usr/local/bin/pdf-service"]
