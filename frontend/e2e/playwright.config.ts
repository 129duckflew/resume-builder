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
})
