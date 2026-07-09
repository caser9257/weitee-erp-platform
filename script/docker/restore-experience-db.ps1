param(
    [switch] $ConfirmRestore,
    [string] $MysqlContainer = "weitee-mysql",
    [string] $MysqlDatabase = "weitee-erp",
    [string] $MysqlRootPassword = "123456",
    [string] $SqlPath,
    [string] $BackupDir
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot = Resolve-Path (Join-Path $ScriptDir "..\..")

if (-not $SqlPath) {
    $SqlPath = Join-Path $RepoRoot "production\ruoyi-vue-pro\weitee-erp_backup_20260709.sql"
}
if (-not $BackupDir) {
    $BackupDir = Join-Path $RepoRoot "production\ruoyi-vue-pro\backups"
}

if (-not $ConfirmRestore) {
    throw "restore is blocked by default because the SQL contains DROP TABLE. Re-run with -ConfirmRestore after confirming the current database must be replaced."
}
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "docker command not found."
}
if (-not (Test-Path -LiteralPath $SqlPath)) {
    throw "database dump not found: $SqlPath"
}

$runningNames = docker ps --format "{{.Names}}"
if ($runningNames -notcontains $MysqlContainer) {
    throw "MySQL container is not running: $MysqlContainer"
}

New-Item -ItemType Directory -Force -Path $BackupDir | Out-Null
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$backupPath = Join-Path $BackupDir "$MysqlDatabase-before-restore-$timestamp.sql"

Write-Host "Backing up current database to: $backupPath"
docker exec -e "MYSQL_PWD=$MysqlRootPassword" $MysqlContainer `
    mysqldump -uroot --single-transaction --routines --triggers --events $MysqlDatabase |
    Set-Content -LiteralPath $backupPath -Encoding UTF8
if ($LASTEXITCODE -ne 0) {
    throw "database backup failed."
}

Write-Host "Restoring experience database from: $SqlPath"
Get-Content -LiteralPath $SqlPath -Raw |
    docker exec -i -e "MYSQL_PWD=$MysqlRootPassword" $MysqlContainer mysql -uroot $MysqlDatabase
if ($LASTEXITCODE -ne 0) {
    throw "database restore failed. Backup kept at: $backupPath"
}

Write-Host "Restore completed. Backup kept at: $backupPath"
