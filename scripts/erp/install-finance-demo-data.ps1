$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = Resolve-Path (Join-Path $scriptDir '..\..')
$runnerFile = Join-Path $scriptDir 'JdbcSqlRunner.java'

$driverCandidates = @(
  'C:\Users\Administrator\.m2\repository\com\mysql\mysql-connector-j\9.6.0\mysql-connector-j-9.6.0.jar',
  'C:\Users\Administrator\.m2\repository\com\mysql\mysql-connector-j\8.0.33\mysql-connector-j-8.0.33.jar'
)

$driverJar = $driverCandidates | Where-Object { Test-Path $_ } | Select-Object -First 1
if (-not $driverJar) {
  throw 'MySQL JDBC driver not found. Please prepare mysql-connector-j in local Maven repository.'
}

if (-not (Test-Path $runnerFile)) {
  throw "Runner source not found: $runnerFile"
}

function Invoke-SqlFile {
  param(
    [Parameter(Mandatory = $true)][string]$RelativePath
  )

  $sqlFile = Join-Path $repoRoot $RelativePath
  if (-not (Test-Path $sqlFile)) {
    throw "SQL script not found: $sqlFile"
  }

  Write-Host "Executing: $RelativePath"
  java --class-path $driverJar $runnerFile $sqlFile
}

$scriptOrder = @(
  'sql\mysql\15-erp-sale-order-reject.sql',
  'sql\mysql\17-erp-sale-order-bpm-lite.sql',
  'sql\mysql\25-erp-purchase-order-bpm-lite.sql',
  'sql\mysql\27-erp-purchase-in-bpm-lite.sql',
  'sql\mysql\30-erp-purchase-in-quality-mvp.sql',
  'sql\mysql\31-erp-purchase-in-stock-in-confirm.sql',
  'sql\mysql\41-erp-sale-delivery-ready-light-quality.sql',
  'sql\mysql\44-erp-purchase-in-partial-stock-execute.sql',
  'sql\mysql\67-erp-purchase-finance-phase1.sql',
  'sql\mysql\68-erp-production-batch-fifo-phase1.sql',
  'sql\mysql\69-erp-purchase-source-batch-phase1.sql',
  'sql\mysql\70-erp-purchase-source-batch-linkage.sql',
  'sql\mysql\71-erp-ap-estimate-phase1.sql',
  'sql\mysql\74-erp-finance-prepayment-phase1.sql',
  'sql\mysql\81-erp-ap-invoice-match-phase1.sql',
  'sql\mysql\82-erp-ap-estimate-closure-phase2.sql',
  'sql\mysql\83-erp-finance-expense-phase1.sql',
  'sql\mysql\85-erp-finance-expense-phase2.sql',
  'sql\mysql\86-erp-finance-ledger-period-phase1.sql',
  'sql\mysql\87-erp-finance-voucher-phase1.sql',
  'sql\mysql\88-erp-finance-voucher-phase2.sql',
  'sql\mysql\89-erp-finance-general-ledger-phase3.sql',
  'sql\mysql\90-erp-finance-subject-report-phase4.sql',
  'sql\mysql\98-erp-finance-personnel-test-users.sql',
  'sql\mysql\99-erp-finance-research-voucher-phase1.sql',
  'sql\mysql\116-erp-finance-dual-ledger-config.sql',
  'sql\mysql\99-erp-finance-demo-data.sql'
)

foreach ($script in $scriptOrder) {
  Invoke-SqlFile -RelativePath $script
}

Write-Host 'Finance demo data bootstrap completed.'
