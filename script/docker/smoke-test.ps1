param(
    [string] $ComposeFile = "script/docker/docker-compose.yml",
    [string] $ServerBaseUrl = "http://localhost:48080",
    [string] $AdminBaseUrl = "http://localhost:8080",
    [string] $LoginUsername = "admin",
    [string] $LoginPassword = "admin123",
    [string] $FixtureFile,
    [switch] $SkipCleanup
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot = Resolve-Path (Join-Path $ScriptDir "..\..")
$ComposePath = Resolve-Path (Join-Path $RepoRoot $ComposeFile)
$ComposeArgs = @("compose", "-f", $ComposePath)
$Passed = 0
$Failed = 0
$Context = @{}

function Invoke-Check {
    param([string] $Name, [scriptblock] $Action)
    try {
        & $Action
        Write-Host "  PASS: $Name" -ForegroundColor Green
        $script:Passed++
    } catch {
        Write-Host "  FAIL: $Name - $($_.Exception.Message)" -ForegroundColor Red
        $script:Failed++
    }
}

function Get-ServiceContainerId {
    param([string] $ServiceName)
    $id = (& docker @ComposeArgs ps -q $ServiceName).Trim()
    if (-not $id) { throw "compose service is not running: $ServiceName" }
    return $id
}

function Wait-ServiceHealth {
    param([string] $ServiceName, [int] $Retries = 60, [int] $Seconds = 2)
    $id = Get-ServiceContainerId $ServiceName
    for ($index = 0; $index -lt $Retries; $index++) {
        $status = (& docker inspect --format "{{.State.Health.Status}}" $id 2>$null).Trim()
        if ($status -eq "healthy") { return }
        Start-Sleep -Seconds $Seconds
    }
    throw "$ServiceName did not become healthy"
}

function Invoke-Api {
    param(
        [ValidateSet("GET", "POST", "PUT", "DELETE")] [string] $Method,
        [string] $Path,
        [object] $Body,
        [string] $Token
    )
    $headers = @{}
    if ($Token) { $headers["Authorization"] = "Bearer $Token" }
    $params = @{
        Uri = "$ServerBaseUrl$Path"
        Method = $Method
        Headers = $headers
        TimeoutSec = 20
        ErrorAction = "Stop"
    }
    if ($null -ne $Body) {
        $params["ContentType"] = "application/json"
        $params["Body"] = $Body | ConvertTo-Json -Depth 20 -Compress
    }
    $response = Invoke-WebRequest @params
    $json = $response.Content | ConvertFrom-Json
    if ($json.code -ne 0) { throw "API error: code=$($json.code), msg=$($json.msg)" }
    return $json
}

function Expand-FixtureJson {
    param([object] $Value)
    if ($null -eq $Value) { return $null }
    $json = $Value | ConvertTo-Json -Depth 20 -Compress
    foreach ($key in $Context.Keys) {
        $json = $json.Replace("{{$key}}", [string] $Context[$key])
    }
    return $json | ConvertFrom-Json
}

Write-Host "=== Docker Supply Chain and Finance Smoke Test ===" -ForegroundColor Cyan

Invoke-Check "Docker command available" {
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) { throw "docker command not found" }
}

if (-not $SkipCleanup) {
    Invoke-Check "Remove existing compose volumes" {
        & docker @ComposeArgs down -v | Out-Null
        if ($LASTEXITCODE -ne 0) { throw "docker compose down failed" }
    }
}

Invoke-Check "Build and start compose services" {
    & docker @ComposeArgs up -d --build | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "docker compose up failed" }
}

Invoke-Check "MySQL healthy" { Wait-ServiceHealth "mysql" }
Invoke-Check "Redis healthy" { Wait-ServiceHealth "redis" }

Invoke-Check "Server health UP" {
    for ($index = 0; $index -lt 60; $index++) {
        try {
            $health = (Invoke-WebRequest -Uri "$ServerBaseUrl/actuator/health" -TimeoutSec 5 -ErrorAction Stop).Content | ConvertFrom-Json
            if ($health.status -eq "UP") { return }
        } catch { }
        Start-Sleep -Seconds 3
    }
    throw "server health endpoint did not return UP"
}

Invoke-Check "Admin page accessible" {
    $response = Invoke-WebRequest -Uri $AdminBaseUrl -TimeoutSec 10 -ErrorAction Stop
    if ($response.StatusCode -ne 200) { throw "admin returned HTTP $($response.StatusCode)" }
}

Invoke-Check "Server logs have no startup blocker" {
    $logs = & docker @ComposeArgs logs server --no-color
    if ($logs -match "UnsupportedClassVersionError|Flyway.*(failed|exception)|SQL.*initialization.*(failed|exception)") {
        throw "server startup blocker found in logs"
    }
}

$token = $null
Invoke-Check "Login and get access token" {
    $login = Invoke-Api "POST" "/admin-api/system/auth/login" @{ username = $LoginUsername; password = $LoginPassword } $null
    $script:token = $login.data.accessToken
    if (-not $script:token) { throw "login response has no accessToken" }
}

$pageApis = @(
    "/admin-api/erp/sale-order/page",
    "/admin-api/erp/purchase-order/page",
    "/admin-api/erp/purchase-in/page",
    "/admin-api/erp/purchase-return/page",
    "/admin-api/erp/stock-in/page",
    "/admin-api/erp/stock-out/page",
    "/admin-api/erp/stock-check/page",
    "/admin-api/erp/stock/page",
    "/admin-api/erp/stock-move/page",
    "/admin-api/erp/finance-payment/page",
    "/admin-api/erp/finance-expense/page",
    "/admin-api/erp/finance-voucher/page",
    "/admin-api/erp/finance-dual-ledger-result/page",
    "/admin-api/erp/product/page",
    "/admin-api/erp/customer/page",
    "/admin-api/erp/supplier/page",
    "/admin-api/erp/warehouse/page"
)

foreach ($path in $pageApis) {
    Invoke-Check "GET $path" {
        Invoke-Api "GET" "$path?pageNo=1&pageSize=1" $null $token | Out-Null
    }
}

if ($FixtureFile) {
    $fixturePath = Resolve-Path $FixtureFile
    $fixture = Get-Content -Raw $fixturePath | ConvertFrom-Json
    foreach ($request in $fixture.requests) {
        Invoke-Check "Fixture: $($request.name)" {
            $body = Expand-FixtureJson $request.body
            $response = Invoke-Api $request.method $request.path $body $token
            if ($request.captureDataAs) {
                $Context[$request.captureDataAs] = $response.data
            }
            if ($request.expectPath) {
                $actual = $response
                foreach ($segment in $request.expectPath.Split('.')) { $actual = $actual.$segment }
                if ([string] $actual -ne [string] $request.expectValue) {
                    throw "expected $($request.expectPath)=$($request.expectValue), actual=$actual"
                }
            }
        }
    }
} else {
    Write-Host "  SKIP: business write smoke requires -FixtureFile with valid account, AP statement, expense type, department, product, unit and warehouse data." -ForegroundColor Yellow
}

Write-Host "=== Result: $Passed passed, $Failed failed ===" -ForegroundColor Cyan
if ($Failed -gt 0) { exit 1 }
