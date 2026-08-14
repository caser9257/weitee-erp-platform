const { chromium } = require('playwright')

;(async () => {
  const browser = await chromium.launch({ headless: true })
  const page = await browser.newPage({ viewport: { width: 1440, height: 900 } })
  page.setDefaultTimeout(25000)

  await page.goto('http://localhost:8888')
  await page.waitForTimeout(5000)
  await page.locator('input[placeholder="请输入账号"]').first().fill('admin')
  await page.locator('input[placeholder="请输入密码"]').first().fill('admin123')
  await page.getByRole('button', { name: '登录' }).first().click()
  await page.waitForTimeout(6000)

  await page.goto('http://localhost:8888/mes/work-task-gantt')
  await page.waitForTimeout(6000)
  await page.locator('.el-select:has-text("请选择工作中心")').first().click({ force: true })
  await page.waitForTimeout(2500)
  await page.locator('.el-select-dropdown__item').first().click({ force: true })
  await page.waitForTimeout(1500)
  const rangeInputs = page.locator('.gantt-query .el-date-editor input')
  await rangeInputs.nth(0).click({ force: true })
  await page.waitForTimeout(500)
  await rangeInputs.nth(0).fill('2026-08-17 00:00:00')
  await page.keyboard.press('Enter')
  await page.waitForTimeout(500)
  await rangeInputs.nth(1).click({ force: true })
  await page.waitForTimeout(500)
  await rangeInputs.nth(1).fill('2026-08-17 23:59:59')
  await page.keyboard.press('Enter')
  await page.waitForTimeout(600)
  await page.locator('button:has-text("查询")').first().click()
  await page.waitForTimeout(6000)

  const info = await page.evaluate(() => {
    const canvas = document.querySelector('.gantt-chart canvas')
    const emptyEl = document.querySelector('.gantt-empty')
    if (!canvas) return { noCanvas: true }
    const ctx = canvas.getContext('2d')
    const img = ctx.getImageData(0, 0, canvas.width, canvas.height).data
    const colorCount = {}
    for (let i = 0; i < img.length; i += 4) {
      const r = img[i], g = img[i + 1], b = img[i + 2], a = img[i + 3]
      if (a > 100 && !(r > 245 && g > 245 && b > 245)) {
        const key = `${r},${g},${b}`
        colorCount[key] = (colorCount[key] || 0) + 1
      }
    }
    const top = Object.entries(colorCount).sort((a, b) => b[1] - a[1]).slice(0, 8)
    return { canvasSize: [canvas.width, canvas.height], emptyVisible: !!emptyEl, topColors: top }
  })
  console.log('PIXEL INFO:', JSON.stringify(info))
  await browser.close()
})().catch((e) => {
  console.error('ERR:', e.message)
  process.exit(2)
})
