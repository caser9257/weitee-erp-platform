$ErrorActionPreference = 'Stop'

$baseUrl = 'http://localhost:18080/admin-api'
$stamp = Get-Date -Format 'yyyyMMddHHmmss'
$marker = "TESTER-SMOKE-STOCK-IN-$stamp"
$headers = @{}
$createdId = $null

function Invoke-Json {
  param(
    [Parameter(Mandatory = $true)][string]$Method,
    [Parameter(Mandatory = $true)][string]$Url,
    [object]$Body = $null
  )

  $params = @{
    Method = $Method
    Uri = $Url
    Headers = $headers
    UseBasicParsing = $true
  }
  if ($null -ne $Body) {
    $params.ContentType = 'application/json'
    $params.Body = ($Body | ConvertTo-Json -Depth 10 -Compress)
  }
  return Invoke-RestMethod @params
}

function Assert-Code0 {
  param(
    [Parameter(Mandatory = $true)]$Response,
    [Parameter(Mandatory = $true)][string]$Step
  )
  if ($Response.code -ne 0) {
    throw "$Step failed: $($Response | ConvertTo-Json -Depth 10 -Compress)"
  }
}

try {
  $login = Invoke-Json -Method 'Post' -Url "$baseUrl/system/auth/login" -Body @{
    username = 'superadmin'
    password = '123456'
    captchaVerification = ''
  }
  Assert-Code0 -Response $login -Step 'login'
  $headers.Authorization = "Bearer $($login.data.accessToken)"

  $create = Invoke-Json -Method 'Post' -Url "$baseUrl/erp/stock-in/create" -Body @{
    supplierId = 980004
    inTime = (Get-Date).ToString('yyyy-MM-dd HH:mm:ss')
    remark = $marker
    items = @(
      @{
        warehouseId = 3
        productId = 3
        productPrice = 1.00
        count = 1
        remark = $marker
      }
    )
  }
  Assert-Code0 -Response $create -Step 'create stock-in'
  $createdId = [long]$create.data

  $afterCreate = Invoke-Json -Method 'Get' -Url "$baseUrl/erp/stock-in/get?id=$createdId"
  Assert-Code0 -Response $afterCreate -Step 'get stock-in after create'

  $submit = Invoke-Json -Method 'Post' -Url "$baseUrl/erp/stock-in/submit" -Body @{
    id = $createdId
    startUserSelectAssignees = @{}
  }
  Assert-Code0 -Response $submit -Step 'submit stock-in'

  $afterSubmit = $null
  for ($i = 0; $i -lt 30; $i++) {
    $afterSubmit = Invoke-Json -Method 'Get' -Url "$baseUrl/erp/stock-in/get?id=$createdId"
    Assert-Code0 -Response $afterSubmit -Step 'get stock-in after submit'
    if ($afterSubmit.data.processInstanceId) {
      break
    }
    if ($afterSubmit.data.status -eq 60) {
      break
    }
    Start-Sleep -Seconds 1
  }
  if (-not $afterSubmit.data.processInstanceId) {
    throw "stock-in submit did not create processInstanceId: $($afterSubmit | ConvertTo-Json -Depth 10 -Compress)"
  }

  $cancel = Invoke-Json -Method 'Delete' -Url "$baseUrl/erp/stock-in/cancel-approval" -Body @{
    id = $createdId
    reason = 'TESTER-SMOKE 自动撤回清理'
  }
  Assert-Code0 -Response $cancel -Step 'cancel stock-in approval'

  $afterCancel = $null
  for ($i = 0; $i -lt 30; $i++) {
    $afterCancel = Invoke-Json -Method 'Get' -Url "$baseUrl/erp/stock-in/get?id=$createdId"
    Assert-Code0 -Response $afterCancel -Step 'get stock-in after cancel'
    if ($afterCancel.data.status -ne 10 -or -not $afterCancel.data.processInstanceId) {
      break
    }
    Start-Sleep -Seconds 1
  }

  $delete = Invoke-Json -Method 'Delete' -Url "$baseUrl/erp/stock-in/delete?ids=$createdId"
  Assert-Code0 -Response $delete -Step 'delete stock-in'
  $createdId = $null

  $finalGet = Invoke-Json -Method 'Get' -Url "$baseUrl/erp/stock-in/get?id=$($afterCreate.data.id)"
  $dataExists = $finalGet.code -eq 0 -and $null -ne $finalGet.data

  [pscustomobject]@{
    marker = $marker
    createdId = $afterCreate.data.id
    createdNo = $afterCreate.data.no
    createStatus = $afterCreate.data.status
    submitCode = $submit.code
    submitData = $submit.data
    submittedStatus = $afterSubmit.data.status
    processInstanceId = $afterSubmit.data.processInstanceId
    cancelCode = $cancel.code
    cancelStatus = $afterCancel.data.status
    deleteCode = $delete.code
    finalGetCode = $finalGet.code
    finalGetMsg = $finalGet.msg
    dataExists = $dataExists
  } | ConvertTo-Json -Depth 10
} finally {
  if ($null -ne $createdId) {
    try {
      Invoke-Json -Method 'Delete' -Url "$baseUrl/erp/stock-in/delete?ids=$createdId" | Out-Null
    } catch {
      Write-Warning "cleanup delete failed for stock-in id=${createdId}: $($_.Exception.Message)"
    }
  }
}
