param(
    [string] $ComposeProjectName = "weitee-system",
    [string] $SqlPath
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot = Resolve-Path (Join-Path $ScriptDir "..\..")

if (-not $SqlPath) {
    $SqlPath = Join-Path $RepoRoot "production\ruoyi-vue-pro\weitee-erp_backup_20260709.sql"
}

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "docker command not found."
}
if (-not (Test-Path -LiteralPath $SqlPath)) {
    throw "database dump not found: $SqlPath"
}

$mysqlVolume = "${ComposeProjectName}_mysql"
$volumeExists = docker volume ls --format "{{.Name}}" | Where-Object { $_ -eq $mysqlVolume }
if ($volumeExists) {
    throw "existing MySQL volume detected: $mysqlVolume. The init SQL will not run automatically on a non-empty MySQL data directory. Use restore-experience-db.ps1 after backing up, or deploy with a fresh volume."
}

Write-Host "OK: preflight passed. MySQL init SQL should run on first startup."
Write-Host "SQL: $SqlPath"
