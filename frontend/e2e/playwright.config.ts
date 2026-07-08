import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './specs',
  timeout: 30000,
  retries: 1,
  use: {
    baseURL: 'http://frontend:80',
    viewport: { width: 1280, height: 720 },
    screenshot: 'only-on-failure',
  },
  webServer: {
    command: 'echo "waiting for services"',
    port: 80,
    timeout: 60000,
    reuseExistingServer: true,
  },
})
