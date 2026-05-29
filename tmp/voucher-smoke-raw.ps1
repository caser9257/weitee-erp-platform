$ErrorActionPreference = 'Stop'
$loginBody = @{ tenantName='管理后台'; username='superadmin'; password='123456' } | ConvertTo-Json
$login = Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:48080/admin-api/system/auth/login' -ContentType 'application/json' -Body $loginBody
$headers = @{ Authorization = "Bearer $($login.data.accessToken)" }
$ledgerRaw = Invoke-WebRequest -Method Get -Uri 'http://127.0.0.1:48080/admin-api/erp/finance-ledger/simple-list' -Headers $headers -UseBasicParsing
$expenseRaw = Invoke-WebRequest -Method Get -Uri 'http://127.0.0.1:48080/admin-api/erp/finance-expense/page?pageNo=1&pageSize=5&status=20' -Headers $headers -UseBasicParsing
"LEDGER>>>$($ledgerRaw.Content)"
"EXPENSE>>>$($expenseRaw.Content)"
