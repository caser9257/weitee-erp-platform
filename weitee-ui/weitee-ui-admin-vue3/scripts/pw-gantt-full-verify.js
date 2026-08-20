const path = require('path')
const { chromium } = require('playwright')

const BASE = 'http://localhost:8888'
const SOP_PAGE_API = '**/admin-api/mes/sop/page*'
const SOP_OCR_API = '**/admin-api/mes/sop-import/ocr*'
const SOP_CREATE_API = '**/admin-api/mes/sop/create*'
const SOP_STATUS_API = '**/admin-api/mes/sop/update-status*'
const SOP_DELETE_API = '**/admin-api/mes/sop/delete*'
const SOP_CONFIRM_IMPORT_API = '**/admin-api/mes/sop-import/confirm*'
const PROCESS_ROUTE_PAGE_API = '**/admin-api/erp/process-route/page*'
const PROCESS_ROUTE_GET_API = '**/admin-api/erp/process-route/get*'
const SCREENSHOT_DIR = path.resolve(__dirname, '../../../output/playwright')
const results = []
const report = (name, ok, extra = '') => {
  results.push({ name, ok, extra })
  console.log(`${ok ? 'PASS' : 'FAIL'} | ${name}${extra ? ' | ' + extra : ''}`)
}

const verifySopPage = async (page) => {
  const waitForSopList = async () => {
    await page.locator('.sop-page__list-card .el-table').waitFor({ state: 'visible' })
    await page.waitForTimeout(2500)
  }

  const processRouteFixture = {
    id: 990003,
    routeCode: 'ROUTE-E2E-001',
    routeName: 'SOP 失败回归路线',
    productId: 990003,
    status: 1,
    steps: [
      {
        id: 990004,
        stepNo: 1,
        stepCode: 'STEP-E2E-001',
        stepName: '失败回归工序'
      }
    ]
  }
  const processRoutePageFixture = async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ code: 0, msg: '', data: { list: [processRouteFixture], total: 1 } })
    })
  }
  const processRouteGetFixture = async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ code: 0, msg: '', data: processRouteFixture })
    })
  }
  await page.route(PROCESS_ROUTE_PAGE_API, processRoutePageFixture)
  await page.route(PROCESS_ROUTE_GET_API, processRouteGetFixture)
  await page.setViewportSize({ width: 1440, height: 900 })
  await page.goto(`${BASE}/mes/sop`)
  await waitForSopList()

  const bodyText = await page.locator('body').innerText()
  const rows = page.locator('.sop-page__list-card .el-table__body-wrapper tbody tr')
  const hasRows = (await rows.count()) > 0
  report('SOP 页标题', /SOP 管理/.test(bodyText))
  report('SOP 列表有数据', hasRows)
  report('SOP 操作入口', /新增 SOP/.test(bodyText) && /OCR 导入/.test(bodyText))
  const statusLabels = bodyText.match(/草稿|已发布|已停用/g) || []
  report('SOP 状态徽章', hasRows && new Set(statusLabels).size > 0)

  let listFailureRequestCount = 0
  const failSopList = async (route) => {
    listFailureRequestCount += 1
    await route.fulfill({
      status: 500,
      contentType: 'application/json',
      body: JSON.stringify({ code: 500, msg: 'SOP 列表测试失败' })
    })
  }
  await page.route(SOP_PAGE_API, failSopList)
  await page.reload()
  const listError = page.locator('.sop-list-error')
  await listError.waitFor({ state: 'visible' })
  report(
    'SOP 列表接口失败态',
    listFailureRequestCount === 1 && /SOP 列表加载失败，请重试/.test(await listError.innerText())
  )
  await page.unroute(SOP_PAGE_API, failSopList)
  await page.getByRole('button', { name: '查询', exact: true }).click()
  await waitForSopList()
  report(
    'SOP 列表失败后可恢复',
    (await page.locator('.sop-ledger__no').count()) > 0 && (await listError.count()) === 0
  )

  let duplicateListRequestCount = 0
  const delaySopList = async (route) => {
    duplicateListRequestCount += 1
    await new Promise((resolve) => setTimeout(resolve, 900))
    await route.continue()
  }
  await page.route(SOP_PAGE_API, delaySopList)
  const duplicateClickDispatched = await page.evaluate(() => {
    const button = Array.from(document.querySelectorAll('.sop-query__actions button')).find(
      (item) => item.textContent?.includes('查询')
    )
    if (!button) return false
    const event = () => new MouseEvent('click', { bubbles: true, cancelable: true, view: window })
    button.dispatchEvent(event())
    button.dispatchEvent(event())
    return true
  })
  await page.waitForTimeout(2200)
  await page.unroute(SOP_PAGE_API, delaySopList)
  report(
    'SOP 查询重复点击防护',
    duplicateClickDispatched && duplicateListRequestCount === 1,
    `requestCount=${duplicateListRequestCount}`
  )

  const firstRow = rows.first()
  const firstSopNo = hasRows ? (await firstRow.locator('.sop-ledger__no').innerText()).trim() : ''
  const sopNoInput = page.locator('.sop-query input').first()
  if (firstSopNo) {
    await sopNoInput.fill(firstSopNo)
    await page.getByRole('button', { name: '查询', exact: true }).click()
    await waitForSopList()
    const queriedNos = await page.locator('.sop-ledger__no').allTextContents()
    report(
      'SOP 编码查询',
      queriedNos.some((value) => value.trim() === firstSopNo)
    )

    await page.getByRole('button', { name: '重置', exact: true }).click()
    await waitForSopList()
    report('SOP 查询重置', (await sopNoInput.inputValue()) === '')
  } else {
    report('SOP 编码查询', false, '无可用 SOP 行')
    report('SOP 查询重置', false, '无可用 SOP 行')
  }

  const viewButton = firstRow.getByRole('button', { name: '查看', exact: true })
  if (hasRows && (await viewButton.count()) > 0) {
    await viewButton.click()
    const detailDialog = page.locator('.el-dialog:visible').filter({ hasText: '查看 SOP' }).first()
    await detailDialog.waitFor({ state: 'visible' })
    let detailLoaded = true
    try {
      await detailDialog.locator('.el-descriptions').waitFor({ state: 'visible' })
    } catch {
      detailLoaded = false
    }
    const detailText = await detailDialog.innerText()
    report('SOP 详情弹窗', detailLoaded && detailText.includes(firstSopNo))
    await detailDialog.getByRole('button', { name: '关闭', exact: true }).click()
    await detailDialog.waitFor({ state: 'hidden' })
  } else {
    report('SOP 详情弹窗', false, '无可用查看入口')
  }

  const createButton = page.getByRole('button', { name: '新增 SOP', exact: true })
  if ((await createButton.count()) > 0) {
    await createButton.click()
    const createDialog = page.locator('.el-dialog:visible').filter({ hasText: '新增 SOP' }).first()
    await createDialog.waitFor({ state: 'visible' })
    const codeInput = createDialog.locator('input[placeholder="如 SOP-001"]')
    report('SOP 新增弹窗', (await codeInput.count()) > 0)
    if ((await codeInput.count()) > 0) {
      await codeInput.fill('SOP-RESET-CHECK')
      const titleInput = createDialog.locator('input[placeholder="请输入 SOP 标题"]')
      const routeSelect = createDialog.locator('.sop-bind .el-select')
      let createReady = (await titleInput.count()) > 0 && (await routeSelect.count()) > 0
      if (createReady) {
        await titleInput.fill('SOP 失败回归')
        await routeSelect.click()
        await page.waitForTimeout(500)
        const routeOption = page
          .locator('.el-select-dropdown__item')
          .filter({ hasText: 'SOP 失败回归路线' })
          .first()
        createReady = (await routeOption.count()) > 0
        if (createReady) {
          await routeOption.click()
          const stepCheckbox = createDialog.locator('.sop-bind__steps .el-checkbox').first()
          try {
            await stepCheckbox.waitFor({ state: 'visible' })
            await stepCheckbox.click()
          } catch {
            createReady = false
          }
        }
      }
      let createFailureRequestCount = 0
      let createDialogClosed = false
      const failSopCreate = async (route) => {
        createFailureRequestCount += 1
        await route.fulfill({
          status: 500,
          contentType: 'application/json',
          body: JSON.stringify({ code: 500, msg: 'SOP 保存测试失败' })
        })
      }
      if (createReady) {
        await page.route(SOP_CREATE_API, failSopCreate)
        await createDialog.getByRole('button', { name: '保存', exact: true }).click()
        const saveError = page.getByText('SOP 保存失败，请重试', { exact: true }).last()
        await saveError.waitFor({ state: 'visible' })
        report(
          'SOP 保存接口失败可恢复',
          createFailureRequestCount === 1 &&
            (await createDialog.isVisible()) &&
            (await codeInput.inputValue()) === 'SOP-RESET-CHECK'
        )
        await page.unroute(SOP_CREATE_API, failSopCreate)
        let createSuccessRequestCount = 0
        const succeedSopCreate = async (route) => {
          createSuccessRequestCount += 1
          await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({ code: 0, msg: '', data: 990010 })
          })
        }
        await page.route(SOP_CREATE_API, succeedSopCreate)
        await createDialog.getByRole('button', { name: '保存', exact: true }).click()
        const createSuccess = page.getByText('创建 SOP 成功', { exact: true }).last()
        await createSuccess.waitFor({ state: 'visible' })
        await createDialog.waitFor({ state: 'hidden' })
        createDialogClosed = true
        report('SOP 保存成功路径', createSuccessRequestCount === 1)
        await page.unroute(SOP_CREATE_API, succeedSopCreate)
      } else {
        report('SOP 保存接口失败可恢复', false, '新增表单缺少可提交的工序选项')
      }
      if (!createDialogClosed) {
        await createDialog.getByRole('button', { name: '取消', exact: true }).click()
        await createDialog.waitFor({ state: 'hidden' })
      }
      await createButton.click()
      const reopenedCreateDialog = page
        .locator('.el-dialog:visible')
        .filter({ hasText: '新增 SOP' })
        .first()
      const reopenedCodeInput = reopenedCreateDialog.locator('input[placeholder="如 SOP-001"]')
      await reopenedCodeInput.waitFor({ state: 'visible' })
      report('SOP 新增弹窗重开清理', (await reopenedCodeInput.inputValue()) === '')
      await reopenedCreateDialog.getByRole('button', { name: '取消', exact: true }).click()
      await reopenedCreateDialog.waitFor({ state: 'hidden' })
    }
  } else {
    report('SOP 新增弹窗', false, '新增权限或入口不可用')
    report('SOP 保存接口失败可恢复', false, '新增权限或入口不可用')
    report('SOP 新增弹窗重开清理', false, '新增权限或入口不可用')
  }

  const statusButton = page.getByRole('button', { name: '停用', exact: true }).first()
  const publishButton = page.getByRole('button', { name: '发布', exact: true }).first()
  const activeStatusButton = (await statusButton.count()) > 0 ? statusButton : publishButton
  const statusAction = (await statusButton.count()) > 0 ? '停用' : '发布'
  if ((await activeStatusButton.count()) > 0) {
    let statusFailureRequestCount = 0
    const failSopStatus = async (route) => {
      statusFailureRequestCount += 1
      await route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ code: 500, msg: 'SOP 状态测试失败' })
      })
    }
    await page.route(SOP_STATUS_API, failSopStatus)
    await activeStatusButton.click()
    const statusConfirm = page
      .locator('.el-message-box:visible')
      .filter({ hasText: `确定${statusAction}` })
      .first()
    await statusConfirm.waitFor({ state: 'visible' })
    await statusConfirm.getByRole('button', { name: '确定', exact: true }).click()
    const statusError = page.getByText(`SOP ${statusAction}失败，请重试`, { exact: true }).last()
    await statusError.waitFor({ state: 'visible' })
    report(
      'SOP 状态更新接口失败可恢复',
      statusFailureRequestCount === 1 && (await activeStatusButton.count()) > 0
    )
    await page.unroute(SOP_STATUS_API, failSopStatus)

    await page.reload()
    await waitForSopList()
    const successStatusButton = page
      .getByRole('button', { name: statusAction, exact: true })
      .first()
    let statusSuccessRequestCount = 0
    const succeedSopStatus = async (route) => {
      statusSuccessRequestCount += 1
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ code: 0, msg: '', data: true })
      })
    }
    await page.route(SOP_STATUS_API, succeedSopStatus)
    await successStatusButton.click()
    const successStatusConfirm = page
      .locator('.el-message-box:visible')
      .filter({ hasText: `确定${statusAction}` })
      .first()
    await successStatusConfirm.waitFor({ state: 'visible' })
    await successStatusConfirm.getByRole('button', { name: '确定', exact: true }).click()
    await page.waitForTimeout(500)
    const statusMessages = await page.locator('.el-message:visible').allTextContents()
    report(
      'SOP 状态更新成功路径',
      statusSuccessRequestCount === 1 &&
        statusMessages.some((message) => message.trim() === `${statusAction}成功`),
      JSON.stringify({ requestCount: statusSuccessRequestCount, statusMessages })
    )
    await page.unroute(SOP_STATUS_API, succeedSopStatus)
  } else {
    report('SOP 状态更新接口失败可恢复', false, '没有可用发布或停用行操作')
  }

  const verifyDeleteFailure = async (deleteButton) => {
    let deleteFailureRequestCount = 0
    const failSopDelete = async (route) => {
      deleteFailureRequestCount += 1
      await route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ code: 500, msg: 'SOP 删除测试失败' })
      })
    }
    await page.route(SOP_DELETE_API, failSopDelete)
    await deleteButton.click()
    const deleteConfirm = page
      .locator('.el-message-box:visible')
      .filter({ hasText: '确定删除 SOP' })
      .first()
    await deleteConfirm.waitFor({ state: 'visible' })
    await deleteConfirm.getByRole('button', { name: '确定', exact: true }).click()
    const deleteError = page.getByText('SOP 删除失败，请重试', { exact: true }).last()
    await deleteError.waitFor({ state: 'visible' })
    report(
      'SOP 删除接口失败可恢复',
      deleteFailureRequestCount === 1 && (await deleteButton.count()) > 0
    )
    await page.unroute(SOP_DELETE_API, failSopDelete)

    let deleteSuccessRequestCount = 0
    const succeedSopDelete = async (route) => {
      deleteSuccessRequestCount += 1
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ code: 0, msg: '', data: true })
      })
    }
    await page.route(SOP_DELETE_API, succeedSopDelete)
    await deleteButton.click()
    const successDeleteConfirm = page
      .locator('.el-message-box:visible')
      .filter({ hasText: '确定删除 SOP' })
      .first()
    await successDeleteConfirm.waitFor({ state: 'visible' })
    await successDeleteConfirm.getByRole('button', { name: '确定', exact: true }).click()
    const deleteSuccess = page.getByText('删除成功', { exact: true }).last()
    await deleteSuccess.waitFor({ state: 'visible' })
    report('SOP 删除成功路径', deleteSuccessRequestCount === 1)
    await page.unroute(SOP_DELETE_API, succeedSopDelete)
  }

  const deleteButton = page.getByRole('button', { name: '删除', exact: true }).first()
  if ((await deleteButton.count()) > 0) {
    await verifyDeleteFailure(deleteButton)
  } else {
    const deleteFixture = {
      id: 990005,
      sopNo: 'SOP-DELETE-FAIL',
      title: '删除失败回归',
      version: 'V1.0',
      status: 0,
      routeStepIds: [],
      createTime: '2026-08-17 00:00:00'
    }
    const sopDeletePageFixture = async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ code: 0, msg: '', data: { list: [deleteFixture], total: 1 } })
      })
    }
    await page.route(SOP_PAGE_API, sopDeletePageFixture)
    await page.reload()
    await waitForSopList()
    const fixtureDeleteButton = page.getByRole('button', { name: '删除', exact: true }).first()
    if ((await fixtureDeleteButton.count()) > 0) {
      await verifyDeleteFailure(fixtureDeleteButton)
    } else {
      report('SOP 删除接口失败可恢复', false, '测试夹具未生成删除行操作')
    }
    await page.unroute(SOP_PAGE_API, sopDeletePageFixture)
    await page.reload()
    await waitForSopList()
  }

  const ocrButton = page.getByRole('button', { name: 'OCR 导入', exact: true })
  if ((await ocrButton.count()) > 0) {
    await ocrButton.click()
    const ocrDialog = page.locator('.el-dialog:visible').filter({ hasText: 'OCR 导入 SOP' }).first()
    await ocrDialog.waitFor({ state: 'visible' })
    const ocrText = await ocrDialog.innerText()
    report('OCR 弹窗打开', /OCR 导入 SOP/.test(ocrText) && /拖拽或点击上传 SOP 图片/.test(ocrText))
    report(
      'OCR 初始状态清洁',
      (await ocrDialog.getByRole('button', { name: '确认生成草稿', exact: true }).count()) === 0
    )
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'sop-full-desktop-ocr.png') })

    let ocrFailureRequestCount = 0
    const failOcrImport = async (route) => {
      ocrFailureRequestCount += 1
      await route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ code: 500, msg: 'OCR 测试失败' })
      })
    }
    await page.route(SOP_OCR_API, failOcrImport)
    const uploadInput = ocrDialog.locator('input[type="file"]')
    await uploadInput.setInputFiles({
      name: 'sop-failure.png',
      mimeType: 'image/png',
      buffer: Buffer.from('sop-failure-test')
    })
    const ocrError = ocrDialog.locator('.el-alert')
    await ocrError.waitFor({ state: 'visible' })
    report(
      'OCR 接口失败态',
      ocrFailureRequestCount === 1 &&
        /OCR 识别失败，请检查 Paddle 服务或重试/.test(await ocrError.innerText())
    )
    await page.unroute(SOP_OCR_API, failOcrImport)
    await ocrDialog.getByRole('button', { name: '关闭', exact: true }).click()
    await ocrDialog.waitFor({ state: 'hidden' })
    await ocrButton.click()
    const reopenedOcrDialog = page
      .locator('.el-dialog:visible')
      .filter({ hasText: 'OCR 导入 SOP' })
      .first()
    await reopenedOcrDialog.waitFor({ state: 'visible' })
    report(
      'OCR 弹窗重开清理',
      (await reopenedOcrDialog
        .getByRole('button', { name: '确认生成草稿', exact: true })
        .count()) === 0 && (await reopenedOcrDialog.locator('.el-alert').count()) === 0
    )

    const succeedOcrImport = async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ code: 0, msg: '', data: { id: 990001, ocrText: '1. 失败回归步骤' } })
      })
    }
    await page.route(SOP_OCR_API, succeedOcrImport)
    await reopenedOcrDialog.locator('input[type="file"]').setInputFiles({
      name: 'sop-confirm-failure.png',
      mimeType: 'image/png',
      buffer: Buffer.from('sop-confirm-failure-test')
    })
    const confirmButton = reopenedOcrDialog.getByRole('button', {
      name: '确认生成草稿',
      exact: true
    })
    await confirmButton.waitFor({ state: 'visible' })
    await page.unroute(SOP_OCR_API, succeedOcrImport)

    let confirmFailureRequestCount = 0
    const failOcrConfirm = async (route) => {
      confirmFailureRequestCount += 1
      await route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ code: 500, msg: 'OCR 草稿测试失败' })
      })
    }
    await page.route(SOP_CONFIRM_IMPORT_API, failOcrConfirm)
    await confirmButton.click()
    const confirmError = page.getByText('SOP 草稿生成失败，请重试', { exact: true }).last()
    await confirmError.waitFor({ state: 'visible' })
    report(
      'OCR 确认接口失败可恢复',
      confirmFailureRequestCount === 1 &&
        (await reopenedOcrDialog.isVisible()) &&
        (await confirmButton.count()) > 0
    )
    await page.unroute(SOP_CONFIRM_IMPORT_API, failOcrConfirm)

    let confirmSuccessRequestCount = 0
    const succeedOcrConfirm = async (route) => {
      confirmSuccessRequestCount += 1
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ code: 0, msg: '', data: 990011 })
      })
    }
    await page.route(SOP_CONFIRM_IMPORT_API, succeedOcrConfirm)
    await confirmButton.click()
    const confirmSuccess = page
      .getByText('已生成 SOP 草稿，可继续编辑后发布', { exact: true })
      .last()
    await confirmSuccess.waitFor({ state: 'visible' })
    await reopenedOcrDialog.waitFor({ state: 'hidden' })
    report('OCR 确认成功路径', confirmSuccessRequestCount === 1)
    await page.unroute(SOP_CONFIRM_IMPORT_API, succeedOcrConfirm)
  } else {
    report('OCR 弹窗打开', false, 'OCR 权限或入口不可用')
    report('OCR 初始状态清洁', false, 'OCR 权限或入口不可用')
    report('OCR 弹窗重开清理', false, 'OCR 权限或入口不可用')
  }

  for (const viewport of [
    { name: '窄桌面', width: 1024, height: 768 },
    { name: '小视口', width: 390, height: 844 }
  ]) {
    await page.setViewportSize({ width: viewport.width, height: viewport.height })
    await page.goto(`${BASE}/mes/sop`)
    await waitForSopList()
    const layout = await page.evaluate(() => {
      const root = document.documentElement
      const filter = document.querySelector('.sop-query__grid')
      return {
        viewportWidth: window.innerWidth,
        documentWidth: root.scrollWidth,
        filterWidth: filter?.getBoundingClientRect().width || 0
      }
    })
    report(
      `SOP ${viewport.name}布局`,
      layout.documentWidth <= layout.viewportWidth + 8 &&
        layout.filterWidth <= layout.viewportWidth + 8,
      JSON.stringify(layout)
    )
    await page.screenshot({
      path: path.join(SCREENSHOT_DIR, `sop-full-${viewport.width}.png`),
      fullPage: true
    })
  }

  await page.setViewportSize({ width: 1440, height: 900 })
  await page.unroute(PROCESS_ROUTE_PAGE_API, processRoutePageFixture)
  await page.unroute(PROCESS_ROUTE_GET_API, processRouteGetFixture)
}

;(async () => {
  const browser = await chromium.launch({ headless: true })
  const page = await browser.newPage({ viewport: { width: 1440, height: 900 } })
  page.setDefaultTimeout(25000)

  let updateCount = 0
  page.on('response', async (resp) => {
    if (resp.url().includes('work-task/update-plan-time')) {
      updateCount++
    }
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
    const token = raw
      ? (() => {
          const v = JSON.parse(raw).v
          return v.startsWith('\"') ? JSON.parse(v) : v
        })()
      : ''
    const resp = await fetch(
      'http://localhost:48080/admin-api/mes/work-task/clear-and-reschedule?productionOrderId=990083',
      {
        method: 'PUT',
        headers: { Authorization: 'Bearer ' + token }
      }
    )
    return { status: resp.status, body: (await resp.text()).slice(0, 100) }
  })
  const prepResult = await page.evaluate(async () => {
    const raw = localStorage.getItem('ACCESS_TOKEN')
    const token = raw
      ? (() => {
          const v = JSON.parse(raw).v
          return v.startsWith('\"') ? JSON.parse(v) : v
        })()
      : ''
    const resp = await fetch(
      'http://localhost:48080/admin-api/mes/work-task/clear-and-reschedule?productionOrderId=990083',
      {
        method: 'PUT',
        headers: { Authorization: 'Bearer ' + token }
      }
    )
    return { status: resp.status, body: (await resp.text()).slice(0, 100) }
  })
  console.log('IDEMPOTENT PREP DONE:', JSON.stringify(prepResult))

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
      const r = img[i],
        g = img[i + 1],
        b = img[i + 2],
        a = img[i + 3]
      if (a < 100) continue
      if (r < 60 && g > 80 && b > 200) counts.blue++
      else if (r < 60 && g > 120 && b < 120) counts.green++
      else if (Math.abs(r - 148) < 10 && Math.abs(g - 163) < 10 && Math.abs(b - 184) < 10)
        counts.gray++
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
  const hitColor = await page.evaluate(
    ([px, py]) => {
      const canvas = document.querySelector('.gantt-chart canvas')
      const r = canvas.getBoundingClientRect()
      const ctx = canvas.getContext('2d')
      const img = ctx.getImageData(Math.round(px - r.left), Math.round(py - r.top), 1, 1).data
      return [img[0], img[1], img[2], img[3]]
    },
    [x, y]
  )
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
  // 跨边界拖拽后块已出画布：先恢复数据并重新查询，再验证拖拽功能仍正常（状态清理效果）
  await page.evaluate(async () => {
    const raw = localStorage.getItem('ACCESS_TOKEN')
    const token = raw
      ? (() => {
          const v = JSON.parse(raw).v
          return v.startsWith('"') ? JSON.parse(v) : v
        })()
      : ''
    await fetch(
      'http://localhost:48080/admin-api/mes/work-task/clear-and-reschedule?productionOrderId=990083',
      {
        method: 'PUT',
        headers: { Authorization: 'Bearer ' + token }
      }
    )
  })
  await query()
  const block2 = await page.evaluate(() => {
    const canvas = document.querySelector('.gantt-chart canvas')
    const r = canvas.getBoundingClientRect()
    const ctx = canvas.getContext('2d')
    const img = ctx.getImageData(0, 0, canvas.width, canvas.height).data
    let minX = 1e9,
      maxX = -1,
      minY = 1e9,
      maxY = -1,
      found = false
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
  report(
    '拖拽状态清理（二次拖拽正常）',
    updateCount > before2,
    `count ${before2} -> ${updateCount}`
  )

  // 恢复演示数据：任务 16 重置回 08-18（避免污染用户体验数据）
  await page.evaluate(async () => {
    const raw = localStorage.getItem('ACCESS_TOKEN')
    const token = raw
      ? (() => {
          const v = JSON.parse(raw).v
          return v.startsWith('\"') ? JSON.parse(v) : v
        })()
      : ''
    await fetch(
      'http://localhost:48080/admin-api/mes/work-task/clear-and-reschedule?productionOrderId=990083',
      {
        method: 'PUT',
        headers: { Authorization: 'Bearer ' + token }
      }
    )
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
      const r = img[i],
        g = img[i + 1],
        b = img[i + 2],
        a = img[i + 3]
      if (a < 100) continue
      if (r < 60 && g > 80 && b > 200) counts.blue++
      else if (r < 60 && g > 120 && b < 120) counts.green++
    }
    return counts
  })
  report(
    '状态筛选后仅已排程（无绿色）',
    pixelFiltered.blue > 5000 && pixelFiltered.green < 500,
    JSON.stringify(pixelFiltered)
  )

  // ===== 5. SOP 页面级回归：列表、查询、详情、弹窗重开和响应式布局 =====
  await verifySopPage(page)

  await browser.close()
  const fails = results.filter((r) => !r.ok)
  console.log(
    `\n===== 甘特 + SOP 综合验证(AGENTS.md 9.1): ${results.length - fails.length}/${results.length} 通过 =====`
  )
  process.exit(fails.length ? 1 : 0)
})().catch((e) => {
  console.error('脚本异常:', e.message)
  process.exit(2)
})
