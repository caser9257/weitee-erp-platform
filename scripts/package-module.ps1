[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string] $Module,

    [string] $Version = (Get-Date -Format "yyyyMMdd-HHmmss"),

    [ValidateSet("prod", "lan", "dev", "test", "stage")]
    [string] $FrontendMode = "prod",

    [string] $SqlPath,
    [string] $ExperienceDoc,
    [switch] $SkipFrontend,
    [switch] $SkipBackend,
    [switch] $SkipTests,
    [switch] $SkipGitCheck
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot = Resolve-Path (Join-Path $ScriptDir "..")
$FrontendRoot = Join-Path $RepoRoot "weitee-ui\weitee-ui-admin-vue3"
$BackendJar = Join-Path $RepoRoot "weitee-server\target\weitee-server.jar"
$OutputRoot = Join-Path $RepoRoot "output\package-work"
$PackageRoot = Join-Path $OutputRoot ("weitee-erp-onmachine-{0}-{1}" -f $Module, $Version)
$ProductionRoot = Join-Path $PackageRoot "production"
$PackageSqlRoot = Join-Path $PackageRoot "sql"
$PackageDocsRoot = Join-Path $PackageRoot "docs"
$PackageScriptRoot = Join-Path $PackageRoot "script"

function Assert-Command([string] $Name) {
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "未找到命令：$Name"
    }
}

function Invoke-Step([string] $Title, [scriptblock] $Action) {
    Write-Host "==> $Title" -ForegroundColor Cyan
    & $Action
    if ($LASTEXITCODE -ne 0) {
        throw "步骤失败：$Title，退出码：$LASTEXITCODE"
    }
}

Assert-Command "git"
if (-not $SkipFrontend) { Assert-Command "pnpm" }
if (-not $SkipBackend) { Assert-Command "mvn" }

if (-not $SkipGitCheck) {
    $status = git -C $RepoRoot status --short
    if ($status) {
        Write-Warning "当前工作区存在未提交变更，脚本仍会继续。请确认这些变更均属于本次模块。"
        $status | ForEach-Object { Write-Host "  $_" }
    }
}

if (Test-Path -LiteralPath $PackageRoot) {
    throw "部署包目录已存在：$PackageRoot。请更换 -Version，避免覆盖历史包。"
}

New-Item -ItemType Directory -Force -Path $ProductionRoot, $PackageSqlRoot, $PackageDocsRoot, $PackageScriptRoot | Out-Null
Copy-Item -Force (Join-Path $ScriptDir "apply-module-migrations.ps1") (Join-Path $PackageScriptRoot "apply-module-migrations.ps1")
Copy-Item -Force (Join-Path $ScriptDir "health-check.ps1") (Join-Path $PackageScriptRoot "health-check.ps1")

if (-not $SkipFrontend) {
    if (-not (Test-Path -LiteralPath (Join-Path $FrontendRoot "package.json"))) {
        throw "前端项目不存在：$FrontendRoot"
    }
    Push-Location $FrontendRoot
    try {
        Invoke-Step "构建前端（mode=$FrontendMode）" {
            pnpm ("build:{0}" -f $FrontendMode)
        }
    } finally {
        Pop-Location
    }

    $FrontendDist = Join-Path $FrontendRoot "dist-prod"
    if (-not (Test-Path -LiteralPath $FrontendDist)) {
        throw "前端构建完成但未找到 dist-prod：$FrontendDist"
    }
    Copy-Item -Recurse -Force $FrontendDist (Join-Path $ProductionRoot "dist-prod")
}

if (-not $SkipBackend) {
    $MavenArgs = @("-DskipTests")
    if (-not $SkipTests) {
        $MavenArgs = @()
    }
    Invoke-Step "构建后端" {
        mvn @MavenArgs clean package
    }
    if (-not (Test-Path -LiteralPath $BackendJar)) {
        throw "后端构建完成但未找到 JAR：$BackendJar"
    }
    Copy-Item -Force $BackendJar (Join-Path $ProductionRoot "weitee-server.jar")
}

if ($SqlPath) {
    $SqlFullPath = Resolve-Path $SqlPath
    Copy-Item -Recurse -Force $SqlFullPath (Join-Path $PackageSqlRoot (Split-Path $SqlFullPath -Leaf))
}

if ($ExperienceDoc) {
    $ExperienceDocFullPath = Resolve-Path $ExperienceDoc
    Copy-Item -Force $ExperienceDocFullPath (Join-Path $PackageDocsRoot (Split-Path $ExperienceDocFullPath -Leaf))
}

$Commit = (git -C $RepoRoot rev-parse --short HEAD).Trim()
$BuildTime = (Get-Date).ToString("s")
$Manifest = @{
    module = $Module
    version = $Version
    buildTime = $BuildTime
    commit = $Commit
    frontendMode = $FrontendMode
    frontendBuilt = (-not $SkipFrontend)
    backendBuilt = (-not $SkipBackend)
    sqlPath = $SqlPath
    experienceDoc = $ExperienceDoc
}
$Manifest | ConvertTo-Json -Depth 5 | Set-Content -Encoding utf8 (Join-Path $PackageRoot "manifest.json")

$Readme = @"
# 微泰 ERP 模块部署包

- 模块：$Module
- 版本：$Version
- 构建时间：$BuildTime
- Git 提交：$Commit
- 前端构建：$(-not $SkipFrontend)
- 后端构建：$(-not $SkipBackend)

## 部署说明

1. 先备份当前运行中的前端、后端和数据库。
2. 执行 `script/apply-module-migrations.ps1` 或手工执行 `sql/` 中本次迁移脚本。
3. 替换 `production/weitee-server.jar` 和/或 `production/dist-prod`。
4. 重启受影响服务。
5. 执行 `script/health-check.ps1` 验证服务、前端和目标模块。

## 目录

- `production/weitee-server.jar`：后端产物（本次构建时生成）
- `production/dist-prod/`：前端产物（本次构建时生成）
- `sql/`：本次模块 SQL
- `docs/`：模块体验说明
- `manifest.json`：构建清单

## 注意

- 本包不包含数据库密码、登录密码或生产环境密钥。
- 如果只构建前端或后端，另一侧目录可能不存在，这是刻意的增量包行为。
"@
$Readme | Set-Content -Encoding utf8 (Join-Path $PackageRoot "README.md")

$ZipPath = "{0}.zip" -f $PackageRoot
Compress-Archive -Path (Join-Path $PackageRoot "*") -DestinationPath $ZipPath -CompressionLevel Optimal
Write-Host "打包完成：$ZipPath" -ForegroundColor Green
