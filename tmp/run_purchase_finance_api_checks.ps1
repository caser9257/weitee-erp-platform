$loginBody = @{
    tenantName = '管理后台'
    username = 'superadmin'
    password = '123456'
} | ConvertTo-Json

$loginResp = Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:48080/admin-api/system/auth/login' `
    -ContentType 'application/json' -Body $loginBody

if ($loginResp.code -ne 0) {
    throw "login failed: $($loginResp | ConvertTo-Json -Depth 10)"
}

$token = $loginResp.data.accessToken
$headers = @{ Authorization = "Bearer $token" }
$baseUrl = 'http://127.0.0.1:48080/admin-api'
$statementId = 1
$supplierId = 2
$paymentId = $null

function Invoke-JsonApi {
    param(
        [string]$Method,
        [string]$Uri,
        $Body
    )

    try {
        if ($PSBoundParameters.ContainsKey('Body')) {
            $jsonBody = $Body | ConvertTo-Json -Depth 10
            $resp = Invoke-WebRequest -Method $Method -Uri $Uri -Headers $headers `
                -ContentType 'application/json' -Body $jsonBody
        } else {
            $resp = Invoke-WebRequest -Method $Method -Uri $Uri -Headers $headers
        }
        $parsed = $null
        if ($resp.Content) {
            $parsed = $resp.Content | ConvertFrom-Json
        }
        return [ordered]@{
            httpStatus = [int]$resp.StatusCode
            body = $parsed
        }
    } catch {
        $response = $_.Exception.Response
        if ($null -eq $response) {
            return [ordered]@{
                error = $_.Exception.Message
            }
        }
        $stream = $response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $content = $reader.ReadToEnd()
        $reader.Close()
        $parsed = $null
        try {
            $parsed = $content | ConvertFrom-Json
        } catch {
        }
        return [ordered]@{
            httpStatus = [int]$response.StatusCode
            body = $parsed
            rawBody = $content
            error = $_.Exception.Message
        }
    }
}

$results = [ordered]@{}
$results.login = [ordered]@{
    httpStatus = 200
    body = $loginResp
}

$results.apPage = Invoke-JsonApi -Method Get -Uri "$baseUrl/erp/ap-statement/page?pageNo=1&pageSize=10"
$results.apGet = Invoke-JsonApi -Method Get -Uri "$baseUrl/erp/ap-statement/get?id=$statementId"
$results.apSummary = Invoke-JsonApi -Method Get -Uri "$baseUrl/erp/ap-statement/summary?supplierId=$supplierId"
$results.apAging = Invoke-JsonApi -Method Get -Uri "$baseUrl/erp/ap-statement/aging?asOfDate=2026-04-27"
$results.apReconciliation = Invoke-JsonApi -Method Get -Uri "$baseUrl/erp/ap-statement/reconciliation?pageNo=1&pageSize=10&supplierId=$supplierId"
$results.apPaymentEnable = Invoke-JsonApi -Method Get -Uri "$baseUrl/erp/ap-statement/payment-enable-page?pageNo=1&pageSize=10&supplierId=$supplierId"
$results.apGetMissing = Invoke-JsonApi -Method Get -Uri "$baseUrl/erp/ap-statement/get?id=9999999"

$results.apInvoiceUpdateReceived = Invoke-JsonApi -Method Put -Uri "$baseUrl/erp/ap-statement/update-invoice" -Body @{
    id = $statementId
    invoiceStatus = 2
    invoiceNo = 'INV-20260427-001'
    invoiceAmount = 28800.00
    remark = 'api-check-received'
}
$results.apGetAfterInvoiceReceived = Invoke-JsonApi -Method Get -Uri "$baseUrl/erp/ap-statement/get?id=$statementId"

$results.apInvoiceUpdateNegative = Invoke-JsonApi -Method Put -Uri "$baseUrl/erp/ap-statement/update-invoice" -Body @{
    id = $statementId
    invoiceStatus = 2
    invoiceNo = 'INV-NEGATIVE'
    invoiceAmount = -1
    remark = 'api-check-negative'
}

$results.apInvoiceReset = Invoke-JsonApi -Method Put -Uri "$baseUrl/erp/ap-statement/update-invoice" -Body @{
    id = $statementId
    invoiceStatus = 0
    invoiceNo = ''
    invoiceAmount = 0
    remark = 'api-check-reset'
}
$results.apGetAfterInvoiceReset = Invoke-JsonApi -Method Get -Uri "$baseUrl/erp/ap-statement/get?id=$statementId"

try {
    $exportResp = Invoke-WebRequest -Method Get -Uri "$baseUrl/erp/ap-statement/export-excel?supplierId=$supplierId" -Headers $headers
    $results.apExport = [ordered]@{
        httpStatus = [int]$exportResp.StatusCode
        contentType = $exportResp.Headers['Content-Type']
        contentDisposition = $exportResp.Headers['Content-Disposition']
        byteLength = ([System.Text.Encoding]::UTF8.GetByteCount($exportResp.Content))
    }
} catch {
    $results.apExport = [ordered]@{
        error = $_.Exception.Message
    }
}

$paymentCreateBody = @{
    paymentTime = '2026-04-27T10:30:00'
    supplierId = 2
    accountId = 2
    discountPrice = 0
    remark = 'api-check-create'
    items = @(
        @{
            apStatementId = $statementId
            bizType = 11
            bizId = 29
            paidPrice = 0
            paymentPrice = 100.00
            remark = 'api-check-item'
        }
    )
}
$results.paymentCreate = Invoke-JsonApi -Method Post -Uri "$baseUrl/erp/finance-payment/create" -Body $paymentCreateBody
if ($results.paymentCreate.body -and $results.paymentCreate.body.code -eq 0) {
    $paymentId = [long]$results.paymentCreate.body.data
}

$results.paymentCreateMissingStatement = Invoke-JsonApi -Method Post -Uri "$baseUrl/erp/finance-payment/create" -Body @{
    paymentTime = '2026-04-27T10:35:00'
    supplierId = 2
    accountId = 2
    discountPrice = 0
    remark = 'api-check-missing'
    items = @(
        @{
            bizType = 11
            bizId = 29
            paidPrice = 0
            paymentPrice = 10.00
        }
    )
}

$results.paymentCreateExceed = Invoke-JsonApi -Method Post -Uri "$baseUrl/erp/finance-payment/create" -Body @{
    paymentTime = '2026-04-27T10:36:00'
    supplierId = 2
    accountId = 2
    discountPrice = 0
    remark = 'api-check-exceed'
    items = @(
        @{
            apStatementId = $statementId
            bizType = 11
            bizId = 29
            paidPrice = 0
            paymentPrice = 999999.00
        }
    )
}

if ($paymentId) {
    $results.paymentPage = Invoke-JsonApi -Method Get -Uri "$baseUrl/erp/finance-payment/page?pageNo=1&pageSize=10"
    $results.paymentGet = Invoke-JsonApi -Method Get -Uri "$baseUrl/erp/finance-payment/get?id=$paymentId"
    $results.paymentApprove = Invoke-JsonApi -Method Put -Uri "$baseUrl/erp/finance-payment/update-status?id=$paymentId&status=20"
    $results.apGetAfterApprove = Invoke-JsonApi -Method Get -Uri "$baseUrl/erp/ap-statement/get?id=$statementId"
    $results.paymentRollback = Invoke-JsonApi -Method Put -Uri "$baseUrl/erp/finance-payment/update-status?id=$paymentId&status=10"
    $results.apGetAfterRollback = Invoke-JsonApi -Method Get -Uri "$baseUrl/erp/ap-statement/get?id=$statementId"
}

$results | ConvertTo-Json -Depth 20
