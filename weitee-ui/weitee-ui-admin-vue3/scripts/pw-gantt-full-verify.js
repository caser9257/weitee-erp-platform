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

  let updateCount = 0
  page.on('response', async (resp) => {
    if (resp.url().includes('work-task/update-plan-time')) updateCount++
  })

  await page.goto(BASE)
  await page.waitForTimeout(5000)
  await page.locator('input[placeholder="请输入账号"]').first().fill('admin')
  await page.locator('input[placeholder="请输入密码"]').first().fill('admin123')
  await page.getByRole('button', { name: '登录' }).first().click()
  await page.waitForTimeout(6000)
  report('登录', !page.url().includes('/login'))
  // 幂等前置：重置演示数据（任务 16 回 08-18），保证每次运行起始状态一致
  await page.evaluate(async () => {
    const raw = localStorage.getItem('ACCESS_TOKEN')
    const token = raw ? JSON.parse(raw).v : ''
    await fetch('http://localhost:48080/admin-api/mes/work-task/clear-and-reschedule?productionOrderId=990083', {
      method: 'PUT',
      headers: { Authorization: 'Bearer ' + token }
    })
  })
  console.log('IDEMPOTENT PREP DONE')

  await page.goto(`${BASE}/mes/work-task-gantt`)
  await page.waitForTimeout(6000)
  await page.locator('.el-select:has-text("请选择工作中心")').first().click({ force: true })
  await page.waitForTimeout(2500)
  await page.locator('.el-select-dropdown__item').first().click({ force: true })
  await page.waitForTimeout(1500)
  const setRange = async (s, e) => {
    const rangeInputs = page.locator('.gantt-query .el-date-editor input')
    await rangeInputs.nth(0).click({ force: true })
    await page.waitForTimeout(400)
    await rangeInputs.nth(0).fill(s)
    await page.keyboard.press('Enter')
    await page.waitForTimeout(400)
    await rangeInputs.nth(1).click({ force: true })
    await page.waitForTimeout(400)
    await rangeInputs.nth(1).fill(e)
    await page.keyboard.press('Enter')
    await page.waitForTimeout(600)
  }
  const query = async () => {
    await page.locator('button:has-text("查询")').first().click()
    await page.waitForTimeout(6000)
  }

  // ===== 2. 默认路径回归：不带状态必须能查到任务（防 eq(null) 回归） =====
  await setRange('2026-08-17 00:00:00', '2026-08-18 23:59:59')
  await query()
  let empty = await page.locator('.gantt-empty').count()
  report('默认路径（不带状态）有数据', empty === 0)

  // ===== 1. 视觉呈现断言（像素级）：canvas 有彩色块（蓝/绿），非灰 =====
  const pixel = await page.evaluate(() => {
    const canvas = document.querySelector('.gantt-chart canvas')
    if (!canvas) return { noCanvas: true }
    const ctx = canvas.getContext('2d')
    const img = ctx.getImageData(0, 0, canvas.width, canvas.height).data
    const counts = { blue: 0, green: 0, gray: 0 }
    for (let i = 0; i < img.length; i += 4) {
      const r = img[i], g = img[i + 1], b = img[i + 2], a = img[i + 3]
      if (a < 100) continue
      if (r < 60 && g > 80 && b > 200) counts.blue++
      else if (r < 60 && g > 120 && b < 120) counts.green++
      else if (Math.abs(r - 148) < 10 && Math.abs(g - 163) < 10 && Math.abs(b - 184) < 10) counts.gray++
    }
    return counts
  })
  report(
    '像素断言：渲染彩色块',
    pixel && !pixel.noCanvas && (pixel.blue > 5000 || pixel.green > 5000),
    JSON.stringify(pixel)
  )
  report('像素断言：无大块灰色', pixel && pixel.gray < 500, `gray=${pixel?.gray}`)

  // ===== 4. 边界交互：拖出画布外再松手（单天范围 08-18，蓝色块占满命中） =====
  await setRange('2026-08-18 00:00:00', '2026-08-18 23:59:59')
  await query()
  const box = await page.locator('canvas').first().boundingBox()
  const x = box.x + 60 + (box.width - 80) * 0.5
  const y = box.y + 30 + (box.height - 70) / 2
  const hitColor = await page.evaluate(([px, py]) => {
    const canvas = document.querySelector('.gantt-chart canvas')
    const r = canvas.getBoundingClientRect()
    const ctx = canvas.getContext('2d')
    const img = ctx.getImageData(Math.round(px - r.left), Math.round(py - r.top), 1, 1).data
    return [img[0], img[1], img[2], img[3]]
  }, [x, y])
  console.log('HIT POINT:', x.toFixed(1), y.toFixed(1), 'color:', JSON.stringify(hitColor))
  await page.mouse.move(x, y)
  await page.mouse.down()
  await page.waitForTimeout(200)
  await page.mouse.move(box.x + box.width + 200, y, { steps: 8 })
  await page.waitForTimeout(300)
  await page.mouse.up()
  await page.waitForTimeout(3000)
  report('跨边界拖拽触发保存', updateCount >= 1, `updateCount=${updateCount}`)

  const before2 = updateCount
  // 重新扫描块位置（跨边界拖拽后块已移位）
  const block2 = await page.evaluate(() => {
    const canvas = document.querySelector('.gantt-chart canvas')
    const r = canvas.getBoundingClientRect()
    const ctx = canvas.getContext('2d')
    const img = ctx.getImageData(0, 0, canvas.width, canvas.height).data
    let minX = 1e9, maxX = -1, minY = 1e9, maxY = -1, found = false
    for (let y = 0; y < canvas.height; y++) {
      for (let x = 0; x < canvas.width; x++) {
        const i = (y * canvas.width + x) * 4
        if (img[i] < 60 && img[i + 1] > 80 && img[i + 2] > 200 && img[i + 3] > 100) {
          found = true
          if (x < minX) minX = x
          if (x > maxX) maxX = x
          if (y < minY) minY = y
          if (y > maxY) maxY = y
        }
      }
    }
    return found ? { x: r.left + (minX + maxX) / 2, y: r.top + (minY + maxY) / 2 } : null
  })
  if (block2) {
    await page.mouse.move(block2.x, block2.y)
    await page.mouse.down()
    await page.waitForTimeout(200)
    await page.mouse.move(block2.x + 60, block2.y, { steps: 6 })
    await page.mouse.up()
    await page.waitForTimeout(3000)
  }
  report('拖拽状态清理（二次拖拽正常）', updateCount > before2, `count ${before2} -> ${updateCount}`)

  // 恢复演示数据：任务 16 重置回 08-18（避免污染用户体验数据）
  await page.evaluate(async () => {
    const raw = localStorage.getItem('ACCESS_TOKEN')
    const token = raw ? JSON.parse(raw).v : ''
    await fetch('http://localhost:48080/admin-api/mes/work-task/clear-and-reschedule?productionOrderId=990083', {
      method: 'PUT',
      headers: { Authorization: 'Bearer ' + token }
    })
  })
  console.log('DEMO DATA RESTORED')

  // ===== 3. 状态筛选默认路径：带 status=1 只剩蓝色 =====
  await page.locator('.gantt-query .el-select:has-text("全部状态")').first().click({ force: true })
  await page.waitForTimeout(1200)
  await page.locator('.el-select-dropdown__item:has-text("已排程")').first().click({ force: true })
  await page.waitForTimeout(600)
  await query()
  const pixelFiltered = await page.evaluate(() => {
    const canvas = document.querySelector('.gantt-chart canvas')
    if (!canvas) return { noCanvas: true }
    const ctx = canvas.getContext('2d')
    const img = ctx.getImageData(0, 0, canvas.width, canvas.height).data
    const counts = { blue: 0, green: 0 }
    for (let i = 0; i < img.length; i += 4) {
      const r = img[i], g = img[i + 1], b = img[i + 2], a = img[i + 3]
      if (a < 100) continue
      if (r < 60 && g > 80 && b > 200) counts.blue++
      else if (r < 60 && g > 120 && b < 120) counts.green++
    }
    return counts
  })
  report('状态筛选后仅已排程（无绿色）', pixelFiltered.blue > 5000 && pixelFiltered.green < 500, JSON.stringify(pixelFiltered))



  await browser.close()
  const fails = results.filter((r) => !r.ok)
  console.log(`\n===== 甘特综合验证(AGENTS.md 9.1): ${results.length - fails.length}/${results.length} 通过 =====`)
  process.exit(fails.length ? 1 : 0)
})().catch((e) => {
  console.error('脚本异常:', e.message)
  process.exit(2)
})
