$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = Resolve-Path (Join-Path $scriptDir '..\..')
$sqlFile = Join-Path $repoRoot 'sql\mysql\local-erp-closure-bootstrap-2026-04-21.sql'
$runnerFile = Join-Path $scriptDir 'JdbcSqlRunner.java'

$driverCandidates = @(
  'C:\Users\Administrator\.m2\repository\com\mysql\mysql-connector-j\9.6.0\mysql-connector-j-9.6.0.jar',
  'C:\Users\Administrator\.m2\repository\com\mysql\mysql-connector-j\8.0.33\mysql-connector-j-8.0.33.jar'
)

$driverJar = $driverCandidates | Where-Object { Test-Path $_ } | Select-Object -First 1
if (-not $driverJar) {
  throw 'MySQL JDBC driver not found. Please prepare mysql-connector-j in local Maven repository.'
}

if (-not (Test-Path $sqlFile)) {
  throw "SQL script not found: $sqlFile"
}

if (-not (Test-Path $runnerFile)) {
  throw "Runner source not found: $runnerFile"
}

Write-Host "Using driver: $driverJar"
Write-Host "Executing script: $sqlFile"

java --class-path $driverJar $runnerFile $sqlFile
