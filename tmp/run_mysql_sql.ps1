param(
    [Parameter(Mandatory = $true)]
    [string]$SqlFilePath,

    [string]$JdbcUrl = 'jdbc:mysql://127.0.0.1:3306/ruoyi-vue-pro?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true&rewriteBatchedStatements=true',
    [string]$Username = 'root',
    [string]$Password = '123456'
)

$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
$runnerClassPath = Join-Path $repoRoot 'tmp'
$driverJar = Join-Path $repoRoot 'tmp\m2repo\com\mysql\mysql-connector-j\8.0.33\mysql-connector-j-8.0.33.jar'
$resolvedSqlFile = (Resolve-Path -LiteralPath $SqlFilePath).Path

if (-not (Test-Path -LiteralPath $driverJar)) {
    throw "MySQL 驱动不存在：$driverJar"
}

java -cp "$runnerClassPath;$driverJar" ApplyMysqlSql $JdbcUrl $Username $Password $resolvedSqlFile
