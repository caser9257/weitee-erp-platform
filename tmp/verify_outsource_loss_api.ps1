$ErrorActionPreference = 'Stop'

$baseUrl = 'http://127.0.0.1:48080/admin-api'
$repoRoot = Split-Path -Parent $PSScriptRoot
$stateFiles = @(
    (Join-Path $repoRoot 'tmp\agent-auth-state.json'),
    (Join-Path $repoRoot 'tmp\iab-state.json')
)

function Get-StateTokens {
    param([string]$StateFilePath)

    if (-not (Test-Path -LiteralPath $StateFilePath)) {
        return @()
    }
    $raw = Get-Content -LiteralPath $StateFilePath -Raw
    $tokens = @()
    $matches = [System.Text.RegularExpressions.Regex]::Matches(
        $raw,
        '"name"\s*:\s*"ACCESS_TOKEN"\s*,\s*"value"\s*:\s*".*?\\\\\\"(?<token>[A-Za-z0-9]{32})\\\\\\""',
        [System.Text.RegularExpressions.RegexOptions]::Singleline
    )
    foreach ($match in $matches) {
        $token = $match.Groups['token'].Value
        if ($token) {
            $tokens += $token
        }
    }
    return $tokens | Select-Object -Unique
}

function Resolve-Headers {
    foreach ($stateFile in $stateFiles) {
        foreach ($token in (Get-StateTokens -StateFilePath $stateFile)) {
            $headers = @{ Authorization = "Bearer $token" }
            try {
                $probe = Invoke-RestMethod -Method Get -Uri "$baseUrl/system/auth/get-permission-info" -Headers $headers
                if ($probe.code -eq 0) {
                    return $headers
                }
            } catch {
            }
        }
    }
    throw 'No usable admin token found in local state files.'
}

function Invoke-Check {
    param(
        [string]$Method,
        [string]$Uri,
        $Body
    )

    try {
        if ($PSBoundParameters.ContainsKey('Body')) {
            $response = Invoke-RestMethod -Method $Method -Uri $Uri -Headers $script:headers `
                -ContentType 'application/json' -Body ($Body | ConvertTo-Json -Depth 10)
        } else {
            $response = Invoke-RestMethod -Method $Method -Uri $Uri -Headers $script:headers
        }
        return [ordered]@{
            ok = $true
            response = $response
        }
    } catch {
        $resp = $_.Exception.Response
        if ($null -ne $resp) {
            try {
                $reader = New-Object System.IO.StreamReader($resp.GetResponseStream())
                $rawBody = $reader.ReadToEnd()
                $reader.Close()
                return [ordered]@{
                    ok = $false
                    httpStatus = [int]$resp.StatusCode
                    rawBody = $rawBody
                    message = $_.Exception.Message
                }
            } catch {
            }
        }
        return [ordered]@{
            ok = $false
            message = $_.Exception.Message
        }
    }
}

$script:headers = Resolve-Headers

$results = [ordered]@{}
$results.tokenProbe = [ordered]@{
    ok = $true
    stateFiles = $stateFiles
}
$results.lossDetailNotExists = Invoke-Check -Method Get -Uri "$baseUrl/erp/outsource-order/loss-detail?orderId=999999999"
$results.lossEntryOrderNotExists = Invoke-Check -Method Post -Uri "$baseUrl/erp/outsource-order/loss-entry/create" -Body @{
    orderId = 999999999
    entries = @(
        @{
            issueBatchId = 1
            lossQty = 1
            remark = 'api-check'
        }
    )
}
$results.lossEntryValidation = Invoke-Check -Method Post -Uri "$baseUrl/erp/outsource-order/loss-entry/create" -Body @{
    orderId = $null
    entries = @()
}

$results | ConvertTo-Json -Depth 20
