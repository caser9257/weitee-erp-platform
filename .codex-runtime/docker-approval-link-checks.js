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
    ['finance-payment', 'http://localhost:8080/finance/payment'],
    ['finance-expense', 'http://localhost:8080/finance/expense']
  ];

  const results = [];
  for (const [key, url] of pages) {
    await page.goto(url, { waitUntil: 'networkidle', timeout: 45000 });
    await page.waitForTimeout(1000);

    const before = await page.evaluate(() => ({
      title: document.title,
      url: location.href,
      approvalRows: Array.from(document.querySelectorAll('.el-table__body tr'))
        .map((row) => row.innerText.replace(/\s+/g, ' ').trim())
        .filter((text) => text.includes('审批中'))
    }));

    const approvalButton = page.getByRole('button', { name: '查看审批' }).first();
    const visible = await approvalButton.isVisible().catch(() => false);
    const enabled = visible ? await approvalButton.isEnabled().catch(() => false) : false;
    let after = null;
    if (enabled) {
      await approvalButton.click();
      await page.waitForLoadState('networkidle', { timeout: 15000 }).catch(() => undefined);
      await page.waitForTimeout(1000);
      after = await page.evaluate(() => ({
        title: document.title,
        url: location.href,
        bodyText: document.body.innerText.replace(/\s+/g, ' ').slice(0, 300),
        isBpmDetail: location.href.includes('/bpm/process-instance/detail') || document.body.innerText.includes('流程') || document.body.innerText.includes('审批')
      }));
      await page.screenshot({ path: 'output/playwright/docker-approval-link-' + key + '.png', fullPage: false });
    }

    results.push({
      key,
      before,
      approvalButtonVisible: visible,
      approvalButtonEnabled: enabled,
      after
    });
  }

  await page.evaluate((results) => {
    window.__dockerApprovalLinkCheckResults = results;
  }, results);
}
