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
    throw new Error('Login through admin container failed: ' + JSON.stringify(loginBody));
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

  const apiResponses = [];
  page.on('response', (response) => {
    const url = response.url();
    if (!url.includes('/admin-api/')) {
      return;
    }
    apiResponses.push({
      status: response.status(),
      url: url.replace(/^http:\/\/localhost:8080/, '')
    });
  });

  const pages = [
    ['sales-order', 'http://localhost:8080/sales/order', '销售订单台账'],
    ['purchase-order', 'http://localhost:8080/scm/purchase-order', '采购订单台账'],
    ['stock-in', 'http://localhost:8080/scm/stock-in', '其他入库'],
    ['stock-out', 'http://localhost:8080/scm/stock-out', '其他出库'],
    ['stock-check', 'http://localhost:8080/scm/stock-check', '库存盘点'],
    ['finance-payment', 'http://localhost:8080/finance/payment', '项目付款管理'],
    ['finance-expense', 'http://localhost:8080/finance/expense', '研发报销 / 零星采购'],
    ['finance-voucher', 'http://localhost:8080/finance/voucher', '财务凭证']
  ];

  const results = [];
  for (const [key, url, expected] of pages) {
    await page.goto(url, { waitUntil: 'networkidle', timeout: 45000 });
    await page.waitForTimeout(2000);
    const title = await page.title();
    const finalUrl = page.url();
    const bodyText = await page.locator('body').innerText({ timeout: 10000 }).catch(() => '');
    await page.screenshot({ path: 'output/playwright/docker-smoke-' + key + '.png', fullPage: false });
    results.push({
      key,
      expected,
      url: finalUrl,
      title,
      hasExpectedText: bodyText.includes(expected),
      isLoginPage: finalUrl.includes('/login') || bodyText.includes('欢迎登录'),
      has404: bodyText.includes('404') || bodyText.includes('页面不存在'),
      textSample: bodyText.replace(/\s+/g, ' ').slice(0, 240)
    });
  }

  console.log(JSON.stringify({
    login: {
      httpStatus: loginResponse.status(),
      code: loginBody.code,
      tokenIssued: Boolean(loginBody.data?.accessToken)
    },
    pages: results,
    apiResponses: apiResponses
      .filter((item) => !item.url.includes('/system/notify-message/get-unread-count'))
      .slice(-80)
  }, null, 2));
}
