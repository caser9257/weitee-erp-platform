async (page) => {
  const loginResponse = await page.request.post('http://localhost:8080/admin-api/system/auth/login', {
    data: {
      username: 'superadmin',
      password: '123456',
      captchaVerification: ''
    }
  });
  const loginBody = await loginResponse.json();
  if (loginBody.code !== 0 || !loginBody.data?.accessToken) {
    throw new Error('Login failed: ' + JSON.stringify(loginBody));
  }

  await page.goto('http://localhost:8080/login', { waitUntil: 'domcontentloaded', timeout: 45000 });
  await page.evaluate((token) => {
    const wrap = (value) => JSON.stringify({
      c: Date.now(),
      e: 253402300799000,
      v: JSON.stringify(value)
    });
    localStorage.setItem('ACCESS_TOKEN', wrap(token.accessToken));
    localStorage.setItem('REFRESH_TOKEN', wrap(token.refreshToken));
  }, loginBody.data);

  const pages = [
    ['sales-order', 'http://localhost:8080/sales/order'],
    ['purchase-order', 'http://localhost:8080/scm/purchase-order'],
    ['stock-in', 'http://localhost:8080/scm/stock-in'],
    ['stock-out', 'http://localhost:8080/scm/stock-out'],
    ['stock-check', 'http://localhost:8080/scm/stock-check'],
    ['finance-payment', 'http://localhost:8080/finance/payment'],
    ['finance-expense', 'http://localhost:8080/finance/expense'],
    ['finance-voucher', 'http://localhost:8080/finance/voucher']
  ];

  const results = [];
  for (const [key, url] of pages) {
    await page.goto(url, { waitUntil: 'networkidle', timeout: 45000 });
    await page.waitForTimeout(1000);

    const queryButton = page.getByRole('button', { name: /查询/ }).first();
    const resetButton = page.getByRole('button', { name: /重置/ }).first();
    const queryVisible = await queryButton.isVisible().catch(() => false);
    const queryEnabled = queryVisible ? await queryButton.isEnabled().catch(() => false) : false;
    const resetVisible = await resetButton.isVisible().catch(() => false);
    const resetEnabled = resetVisible ? await resetButton.isEnabled().catch(() => false) : false;
    if (queryEnabled) {
      await queryButton.click();
      await page.waitForLoadState('networkidle', { timeout: 15000 }).catch(() => undefined);
      await page.waitForTimeout(500);
    }

    const pageInfo = await page.evaluate(() => {
      const text = document.body.innerText;
      const rows = Array.from(document.querySelectorAll('.el-table__body tr'))
        .map((row) => row.innerText.replace(/\s+/g, ' ').trim())
        .filter(Boolean);
      const approvalRows = rows.filter((row) => row.includes('审批中'));
      const failedRows = rows.filter((row) => row.includes('处理失败'));
      const generatedRows = rows.filter((row) => row.includes('已生成'));
      return {
        title: document.title,
        url: location.href,
        hasAmountWithThousands: /\d{1,3}(,\d{3})+\.\d{2}/.test(text),
        hasAnyMoney2dp: /\b-?\d+\.\d{2}\b/.test(text),
        hasBadTrailingZeroQty: /\b-?\d+\.000\b/.test(text),
        hasApprovalText: text.includes('审批中'),
        hasFailedText: text.includes('处理失败'),
        hasGeneratedVoucherText: text.includes('已生成'),
        approvalRows,
        failedRows,
        generatedRows: generatedRows.slice(0, 3),
        rowCount: rows.length,
        emptyState: text.includes('暂无数据') || text.includes('暂无记录') || text.includes('无数据')
      };
    });

    await page.screenshot({ path: 'output/playwright/docker-page-check-' + key + '.png', fullPage: false });
    results.push({
      key,
      queryVisible,
      queryEnabled,
      resetVisible,
      resetEnabled,
      ...pageInfo,
      approvalRowsExposeEditOrDelete: pageInfo.approvalRows.some((row) => /编辑|删除/.test(row)),
      failedRowsExposeSubmit: pageInfo.failedRows.some((row) => /提交审批|重新提交|提交/.test(row))
    });
  }

  await page.evaluate((results) => {
    window.__dockerPageCheckResults = results;
  }, results);
}
