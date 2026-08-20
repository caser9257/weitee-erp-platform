[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string] $ModulePath,

    [string] $Database = "weitee",
    [string] $DbHost = "127.0.0.1",
    [int] $Port = 3306,
    [string] $User = "root",
    [SecureString] $Password,
    [switch] $DryRun
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot = Resolve-Path (Join-Path $ScriptDir "..")

if (-not $DryRun -and -not (Get-Command mysql -ErrorAction SilentlyContinue)) {
    throw "未找到 mysql 命令。请安装 MySQL 客户端，或在部署包中使用容器内 mysql 客户端执行。"
}

$MigrationRoot = Resolve-Path (Join-Path $RepoRoot $ModulePath)
$MigrationFiles = @(Get-ChildItem -LiteralPath $MigrationRoot -Filter "*.sql" -File | Sort-Object Name)
if ($MigrationFiles.Count -eq 0) {
    Write-Host "迁移目录当前没有待执行 SQL，跳过：$MigrationRoot" -ForegroundColor Yellow
    exit 0
}

function Convert-SecureStringToPlainText([SecureString] $Value) {
    if (-not $Value) { return "" }
    $BSTR = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($Value)
    try { return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($BSTR) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($BSTR) }
}

$PlainPassword = Convert-SecureStringToPlainText $Password
$Env:MYSQL_PWD = $PlainPassword
try {
    $BootstrapSql = @"
CREATE TABLE IF NOT EXISTS weitee_schema_version (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  script_name VARCHAR(255) NOT NULL,
  checksum CHAR(64) NOT NULL,
  executed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_wei_schema_version_script (script_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
"@

    if (-not $DryRun) {
        $BootstrapSql | mysql --protocol=tcp --host=$DbHost --port=$Port --user=$User $Database
        if ($LASTEXITCODE -ne 0) { throw "创建迁移记录表失败：$Database" }
    }

    foreach ($File in $MigrationFiles) {
        $Checksum = (Get-FileHash -Algorithm SHA256 -LiteralPath $File.FullName).Hash.ToLowerInvariant()
        $EscapedName = $File.Name.Replace("'", "''")
        $Existing = ""
        if (-not $DryRun) {
            $Existing = mysql --protocol=tcp --host=$DbHost --port=$Port --user=$User --batch --skip-column-names $Database -e "SELECT checksum FROM weitee_schema_version WHERE script_name = '$EscapedName' LIMIT 1;"
            if ($LASTEXITCODE -ne 0) { throw "查询迁移记录失败：$($File.Name)" }
            $Existing = ($Existing | Out-String).Trim()
        }

        if ($Existing) {
            if ($Existing -ne $Checksum) {
                throw "迁移脚本内容已变化，拒绝继续：$($File.Name)。已执行脚本不能直接修改，请新增版本脚本。"
            }
            Write-Host "SKIP $($File.Name)（已执行）" -ForegroundColor DarkGray
            continue
        }

        if ($DryRun) {
            Write-Host "DRY-RUN $($File.Name) [$Checksum]" -ForegroundColor Yellow
            continue
        }

        Write-Host "APPLY $($File.Name)" -ForegroundColor Cyan
        Get-Content -Raw -LiteralPath $File.FullName | mysql --protocol=tcp --host=$DbHost --port=$Port --user=$User $Database
        if ($LASTEXITCODE -ne 0) { throw "执行迁移失败：$($File.Name)" }
        mysql --protocol=tcp --host=$DbHost --port=$Port --user=$User $Database -e "INSERT INTO weitee_schema_version(script_name, checksum) VALUES ('$EscapedName', '$Checksum');"
        if ($LASTEXITCODE -ne 0) { throw "写入迁移记录失败：$($File.Name)" }
    }
} finally {
    Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
}

Write-Host "模块迁移处理完成：$ModulePath" -ForegroundColor Green
