const { chromium } = require('playwright')

const BASE = 'http://localhost:8888'
const results = []
const report = (name, ok, extra = '') => {
  results.push({ name, ok, extra })
  console.log(`${ok ? 'PASS' : 'FAIL'} | ${name}${extra ? ' | ' + extra : ''}`)
}

;(async () => {
  const browser = await chromium.launch({ headless: true })
  const page = await browser.newPage({ viewport: { width: 1440, height: 900 } })
  page.setDefaultTimeout(25000)

  await page.goto(BASE)
  await page.waitForTimeout(5000)
  await page.locator('input[placeholder="请输入账号"]').first().fill('admin')
  await page.locator('input[placeholder="请输入密码"]').first().fill('admin123')
  await page.getByRole('button', { name: '登录' }).first().click()
  await page.waitForTimeout(6000)
  report('登录', !page.url().includes('/login'))

  await page.goto(`${BASE}/mes/sop`)
  await page.waitForTimeout(6000)
  const text = await page.locator('body').innerText().catch(() => '')
  report('SOP 页标题', /SOP 管理/.test(text))
  report('新增按钮', /新增 SOP/.test(text))
  report('OCR 导入按钮', /OCR 导入/.test(text))
  report('SOP 数据渲染', /SOP-E2E-001/.test(text) && /已发布/.test(text))
  report('状态徽章', /SOP-OCR-E2E/.test(text) && /草稿/.test(text))
  await page.screenshot({ path: 'C:/Users/Administrator/AppData/Local/Temp/tester/pw-shots/50-sop.png' })

  // OCR 弹窗
  await page.locator('button:has-text("OCR 导入")').first().click()
  await page.waitForTimeout(2000)
  const dialogText = await page.locator('.el-dialog').first().innerText().catch(() => '')
  report('OCR 弹窗打开', /OCR 导入 SOP/.test(dialogText))
  await page.screenshot({ path: 'C:/Users/Administrator/AppData/Local/Temp/tester/pw-shots/51-sop-ocr.png' })

  await browser.close()
  const fails = results.filter((r) => !r.ok)
  console.log(`\n===== SOP 页面验证: ${results.length - fails.length}/${results.length} 通过 =====`)
  process.exit(fails.length ? 1 : 0)
})().catch((e) => {
  console.error('脚本异常:', e.message)
  process.exit(2)
})
