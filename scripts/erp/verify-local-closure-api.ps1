param(
  [string]$BaseUrl = 'http://127.0.0.1:48080',
  [string]$Username = 'superadmin',
  [string]$Password = '123456'
)

$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$loginBody = @{
  username = $Username
  password = $Password
  captchaVerification = ''
} | ConvertTo-Json

$loginResp = Invoke-RestMethod `
  -Uri "$BaseUrl/admin-api/system/auth/login" `
  -Method Post `
  -ContentType 'application/json' `
  -Body $loginBody

if ($loginResp.code -ne 0 -or -not $loginResp.data.accessToken) {
  throw "Login failed: $($loginResp | ConvertTo-Json -Depth 10)"
}

$token = $loginResp.data.accessToken
$pageResp = Invoke-RestMethod `
  -Uri "$BaseUrl/admin-api/erp/sale-order/get-closure-summary-page?pageNo=1&pageSize=10" `
  -Method Get `
  -Headers @{ Authorization = "Bearer $token" }

if ($pageResp.code -ne 0) {
  throw "Closure page API failed: $($pageResp | ConvertTo-Json -Depth 10)"
}

$summary = [PSCustomObject]@{
  total = $pageResp.data.total
  firstOrderNo = $pageResp.data.list[0].no
  firstClosureStage = $pageResp.data.list[0].closureSummary.closureStage
  firstBlockers = ($pageResp.data.list[0].closureSummary.blockerCodes -join ',')
}

$summary | ConvertTo-Json -Depth 10
