const { chromium } = require('playwright')

;(async () => {
  const browser = await chromium.launch({ headless: true })
  const page = await browser.newPage({ viewport: { width: 1440, height: 900 } })
  page.setDefaultTimeout(20000)

  let updateCalled = false
  let updateBody = ''
  page.on('response', async (resp) => {
    if (resp.url().includes('work-task/update-plan-time')) {
      updateCalled = true
      updateBody = (await resp.text().catch(() => '')).slice(0, 120)
    }
  })

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
  // 查询范围：08-17 全天（任务排程在此）
  const rangeInputs = page.locator('.gantt-query .el-date-editor input')
  await rangeInputs.nth(0).click({ force: true })
  await page.waitForTimeout(600)
  await rangeInputs.nth(0).fill('2026-08-17 00:00:00')
  await page.keyboard.press('Enter')
  await page.waitForTimeout(600)
  await rangeInputs.nth(1).click({ force: true })
  await page.waitForTimeout(600)
  await rangeInputs.nth(1).fill('2026-08-17 23:59:59')
  await page.keyboard.press('Enter')
  await page.waitForTimeout(800)
  await page.locator('button:has-text("查询")').first().click()
  await page.waitForTimeout(6000)

  const box = await page.locator('canvas').first().boundingBox()
  const plotLeft = box.x + 60
  const plotWidth = box.width - 80
  const plotTop = box.y + 30
  const plotHeight = box.height - 70
  const y = plotTop + plotHeight / 2
  // 任务 08-17 08:00~16:00：左 33%、右 67%
  const leftPx = plotLeft + plotWidth * (8 / 24)
  const rightPx = plotLeft + plotWidth
  console.log('RESIZE RIGHT EDGE AT:', rightPx.toFixed(1), y.toFixed(1))

  // 右边缘拉伸：mousedown 在右边缘 → 右移 120px（≈ +3h）→ mouseup
  await page.mouse.move(rightPx, y)
  await page.mouse.down()
  await page.waitForTimeout(200)
  await page.mouse.move(rightPx - 120, y, { steps: 8 })
  await page.waitForTimeout(300)
  await page.mouse.up()
  await page.waitForTimeout(3500)

  console.log('UPDATE:', updateCalled ? updateBody : 'NOT CALLED')
  console.log('RESULT:', updateCalled ? 'PASS - resize triggered update-plan-time' : 'FAIL')
  await browser.close()
  process.exit(updateCalled ? 0 : 1)
})().catch((e) => {
  console.error('ERR:', e.message)
  process.exit(2)
})
