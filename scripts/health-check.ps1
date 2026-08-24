[CmdletBinding()]
param(
    [string] $ConfigPath,
    [string] $BaseUrl = "http://127.0.0.1:48080",
    [string] $FrontendUrl = "http://127.0.0.1:8080",
    [string] $LoginUsername,
    [SecureString] $LoginPassword,
    [string] $CaptchaVerification,
    [string] $RequiredMenuPath,
    [string] $BusinessUrl,
    [string[]] $BusinessUrls,
    [int] $TimeoutSeconds = 10
)

$ErrorActionPreference = "Stop"
$Failures = [System.Collections.Generic.List[string]]::new()

if ($ConfigPath) {
    $ConfigFullPath = Resolve-Path $ConfigPath
    $Config = Get-Content -Raw -LiteralPath $ConfigFullPath | ConvertFrom-Json
    if ($Config.baseUrl) { $BaseUrl = $Config.baseUrl }
    if ($Config.frontendUrl) { $FrontendUrl = $Config.frontendUrl }
    if ($Config.loginUsername) { $LoginUsername = $Config.loginUsername }
    if ($Config.requiredMenuPath) { $RequiredMenuPath = $Config.requiredMenuPath }
    if ($Config.businessUrl) { $BusinessUrl = $Config.businessUrl }
    if ($Config.businessUrls) { $BusinessUrls = @($Config.businessUrls) }
}

function Test-Http([string] $Name, [string] $Url, [hashtable] $Headers = @{}, [object] $Body = $null) {
    try {
        $Params = @{
            Uri = $Url
            Method = if ($null -eq $Body) { "Get" } else { "Post" }
            Headers = $Headers
            TimeoutSec = $TimeoutSeconds
            SkipHttpErrorCheck = $true
        }
        if ($null -ne $Body) {
            $Params.ContentType = "application/json"
            $Params.Body = ($Body | ConvertTo-Json -Compress)
        }
        $Response = Invoke-WebRequest @Params
        if ($Response.StatusCode -lt 200 -or $Response.StatusCode -ge 300) {
            throw "HTTP $($Response.StatusCode)"
        }
        Write-Host "PASS $Name [$($Response.StatusCode)] $Url" -ForegroundColor Green
        return $Response
    } catch {
        $Message = "FAIL $Name $Url：$($_.Exception.Message)"
        Write-Host $Message -ForegroundColor Red
        $Failures.Add($Message)
        return $null
    }
}

$HealthResponse = Test-Http "后端健康状态" "$BaseUrl/actuator/health"
if ($HealthResponse) {
    try {
        $HealthJson = $HealthResponse.Content | ConvertFrom-Json
        if ($HealthJson.status -ne "UP") {
            throw "Actuator 状态为 $($HealthJson.status)，不是 UP"
        }
    } catch {
        $Message = "FAIL 后端健康内容校验：$($_.Exception.Message)"
        Write-Host $Message -ForegroundColor Red
        $Failures.Add($Message)
    }
}
Test-Http "后端接口文档" "$BaseUrl/v3/api-docs" | Out-Null
Test-Http "前端入口" $FrontendUrl | Out-Null

$Headers = @{}
if ($LoginUsername -or $LoginPassword) {
    if (-not $LoginUsername -or -not $LoginPassword) {
        throw "登录检查需要同时提供 -LoginUsername 和 -LoginPassword。"
    }
    $BSTR = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($LoginPassword)
    try { $PlainPassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($BSTR) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($BSTR) }

    $LoginResponse = Test-Http "管理端登录" "$BaseUrl/admin-api/system/auth/login" @{} @{ username = $LoginUsername; password = $PlainPassword; captchaVerification = $CaptchaVerification }
    if ($LoginResponse) {
        try {
            $LoginJson = $LoginResponse.Content | ConvertFrom-Json
            $Token = $LoginJson.data.accessToken
            if (-not $Token) { throw "登录响应中没有 data.accessToken" }
            $Headers = @{ Authorization = "Bearer $Token" }
            Test-Http "登录用户信息" "$BaseUrl/admin-api/system/auth/get-permission-info" $Headers | Out-Null
            if ($RequiredMenuPath) {
                $PermissionResponse = Test-Http "菜单权限" "$BaseUrl/admin-api/system/auth/get-permission-info" $Headers
                if ($PermissionResponse -and $PermissionResponse.Content -notlike "*$RequiredMenuPath*") {
                    throw "登录用户菜单中未找到：$RequiredMenuPath"
                }
            }
        } catch {
            $Message = "FAIL 登录响应校验：$($_.Exception.Message)"
            Write-Host $Message -ForegroundColor Red
            $Failures.Add($Message)
        }
    }
}

if ($BusinessUrl) {
    if (-not $Headers.Authorization) {
        throw "检查业务接口需要先提供登录参数。"
    }
    Test-Http "模块业务接口" $BusinessUrl $Headers | Out-Null
}

if ($BusinessUrls) {
    if (-not $Headers.Authorization) {
        throw "检查业务接口需要先提供登录参数。"
    }
    foreach ($Url in $BusinessUrls) {
        Test-Http "模块业务接口" $Url $Headers | Out-Null
    }
}

if ($Failures.Count -gt 0) {
    Write-Host "健康检查失败，共 $($Failures.Count) 项。" -ForegroundColor Red
    exit 1
}
Write-Host "健康检查通过。" -ForegroundColor Green
