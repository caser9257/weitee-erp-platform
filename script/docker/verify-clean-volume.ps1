param(
    [string] $ComposeProjectName = "weitee-system",
    [string] $ComposeFile = "script/docker/docker-compose.yml",
    [string] $VerifyComposeFile = "script/docker/docker-compose.verify.yml",
    [string] $ServerBaseUrl = "http://localhost:48080",
    [switch] $SkipCleanup
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot = Resolve-Path (Join-Path $ScriptDir "..\..")
$ComposePath = Resolve-Path (Join-Path $RepoRoot $ComposeFile)
$VerifyComposePath = Resolve-Path (Join-Path $RepoRoot $VerifyComposeFile)
$env:COMPOSE_PROJECT_NAME = $ComposeProjectName

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "docker command not found."
}
if (-not (Get-Command curl -ErrorAction SilentlyContinue) -and (-not (Get-Command Invoke-WebRequest -ErrorAction SilentlyContinue))) {
    Write-Host "WARNING: curl not found. Will use PowerShell Invoke-WebRequest for HTTP checks."
}

Write-Host "========================================"
Write-Host "Docker Clean Volume Verification"
Write-Host "========================================"

# Step 1: Stop and clean up
Write-Host "`n[1/5] Cleaning up existing containers and volumes..."
$running = docker compose ls --format "{{.Name}}" | Where-Object { $_ -eq $ComposeProjectName }
if ($running) {
    docker compose -f $ComposePath down -v
    if ($LASTEXITCODE -ne 0) { throw "docker compose down failed." }
}
Write-Host "  OK: containers and volumes removed."

# Step 2: Build and start
Write-Host "`n[2/5] Building and starting services..."
docker compose -f $ComposePath up -d --build
if ($LASTEXITCODE -ne 0) { throw "docker compose up failed." }
Write-Host "  OK: services starting."

# Step 3: Wait for MySQL health
Write-Host "`n[3/5] Waiting for MySQL health..."
$mysqlOk = $false
for ($i = 0; $i -lt 60; $i++) {
    $status = docker inspect --format "{{.State.Health.Status}}" "${ComposeProjectName}-mysql-1" 2>$null
    if ($status -eq "healthy") { $mysqlOk = $true; break }
    Start-Sleep -Seconds 2
}
if (-not $mysqlOk) { throw "MySQL not healthy after 120s." }
Write-Host "  OK: MySQL healthy."

# Step 4: Wait for Redis health
Write-Host "`n[3b/5] Waiting for Redis health..."
$redisOk = $false
for ($i = 0; $i -lt 30; $i++) {
    $status = docker inspect --format "{{.State.Health.Status}}" "${ComposeProjectName}-redis-1" 2>$null
    if ($status -eq "healthy") { $redisOk = $true; break }
    Start-Sleep -Seconds 2
}
if (-not $redisOk) { throw "Redis not healthy after 60s." }
Write-Host "  OK: Redis healthy."

# Step 5: Wait for server health
Write-Host "`n[3c/5] Waiting for server health..."
$serverOk = $false
for ($i = 0; $i -lt 60; $i++) {
    try {
        $resp = Invoke-WebRequest -Uri "${ServerBaseUrl}/actuator/health" -TimeoutSec 5 -ErrorAction Stop
        $json = $resp.Content | ConvertFrom-Json
        if ($json.status -eq "UP") { $serverOk = $true; break }
    } catch {}
    Start-Sleep -Seconds 3
}
if (-not $serverOk) { throw "Server not healthy after 180s." }
Write-Host "  OK: Server health UP."

# Step 6: Verify core page APIs
Write-Host "`n[4/5] Verifying core page APIs..."
$corePages = @(
    "/admin-api/erp/sale-order/page",
    "/admin-api/erp/purchase-order/page",
    "/admin-api/erp/stock-in/page",
    "/admin-api/erp/stock-out/page",
    "/admin-api/erp/stock-check/page",
    "/admin-api/erp/finance-payment/page",
    "/admin-api/erp/finance-expense/page",
    "/admin-api/erp/finance-voucher/page"
)
$failed = 0
foreach ($page in $corePages) {
    try {
        $resp = Invoke-WebRequest -Uri "${ServerBaseUrl}${page}?pageNo=1&pageSize=1" -TimeoutSec 10 -ErrorAction Stop
        $json = $resp.Content | ConvertFrom-Json
        if ($json.code -eq 0) {
            Write-Host "  OK: $page"
        } else {
            Write-Host "  FAIL: $page (code=$($json.code))"
            $failed++
        }
    } catch {
        Write-Host "  FAIL: $page ($($_.Exception.Message))"
        $failed++
    }
}

# Step 7: Report
Write-Host "`n[5/5] Results..."
if ($failed -eq 0) {
    Write-Host "========================================"
    Write-Host " CLEAN VOLUME VERIFICATION PASSED"
    Write-Host " All $($corePages.Count) pages accessible."
    Write-Host "========================================"
} else {
    Write-Host "========================================"
    Write-Host " CLEAN VOLUME VERIFICATION FAILED"
    Write-Host " $failed / $($corePages.Count) checks failed."
    Write-Host "========================================"
    exit 1
}