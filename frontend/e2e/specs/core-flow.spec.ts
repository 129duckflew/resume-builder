import { test, expect } from '@playwright/test'

test.describe('Core resume flow', () => {
  test('register, create resume, edit, and preview', async ({ page }) => {
    // Register
    await page.goto('/register')
    const username = `testuser_${Date.now()}`
    await page.fill('input[name="username"]', username)
    await page.fill('input[name="email"]', `${username}@test.com`)
    await page.fill('input[name="password"]', 'password123')
    await page.click('button[type="submit"]')
    await expect(page).toHaveURL(/\/$/)

    // Create resume
    await page.click('text=New Resume')
    await expect(page).toHaveURL(/\/editor\//)
    await page.waitForTimeout(1000)

    // Edit content
    const editor = page.locator('.w-md-editor-text-input')
    await expect(editor).toBeVisible()
    await editor.fill('# Test Title\n\nHello world')

    // Wait for save
    await page.waitForTimeout(1500)

    // Preview
    await page.goto(`/preview/${page.url().split('/').pop()}`)
    await page.waitForTimeout(1000)
  })

  test('homepage loads without auth redirects to login', async ({ page }) => {
    await page.goto('/')
    await expect(page).toHaveURL(/\/login/)
  })
})
