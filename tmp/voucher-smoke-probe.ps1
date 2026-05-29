$ErrorActionPreference = 'Stop'
$loginBody = @{ tenantName='管理后台'; username='superadmin'; password='123456' } | ConvertTo-Json
$login = Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:48080/admin-api/system/auth/login' -ContentType 'application/json' -Body $loginBody
$headers = @{ Authorization = "Bearer $($login.data.accessToken)" }
$ledger = Invoke-RestMethod -Method Get -Uri 'http://127.0.0.1:48080/admin-api/erp/finance-ledger/simple-list' -Headers $headers
$expensePage = Invoke-RestMethod -Method Get -Uri 'http://127.0.0.1:48080/admin-api/erp/finance-expense/page?pageNo=1&pageSize=5&status=20' -Headers $headers
[pscustomobject]@{
  ledgerCount = ($ledger.data | Measure-Object).Count
  firstLedgerId = if ($ledger.data) { $ledger.data[0].id } else { $null }
  expenseCount = $expensePage.data.total
  firstExpenseId = if ($expensePage.data.list) { $expensePage.data.list[0].id } else { $null }
  firstExpenseNo = if ($expensePage.data.list) { $expensePage.data.list[0].no } else { $null }
} | ConvertTo-Json -Depth 5
