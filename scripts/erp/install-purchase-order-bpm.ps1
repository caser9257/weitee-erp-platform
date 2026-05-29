$ErrorActionPreference = 'Stop'

$baseUrl = if ($env:YUDAO_BASE_URL) { $env:YUDAO_BASE_URL.TrimEnd('/') } else { 'http://127.0.0.1:48080/admin-api' }
$tenantId = if ($env:YUDAO_TENANT_ID) { $env:YUDAO_TENANT_ID } else { '1' }
$mockUserId = if ($env:YUDAO_MOCK_USER_ID) { $env:YUDAO_MOCK_USER_ID } else { '1' }
$authToken = if ($env:YUDAO_AUTH_TOKEN) { $env:YUDAO_AUTH_TOKEN } else { "Bearer test$mockUserId" }

$headers = @{
  Authorization = $authToken
  'tenant-id' = $tenantId
}

function Invoke-YudaoJson {
  param(
    [Parameter(Mandatory = $true)][string]$Method,
    [Parameter(Mandatory = $true)][string]$Path,
    [object]$Body = $null
  )

  $params = @{
    Method = $Method
    Uri = "$baseUrl$Path"
    Headers = $headers
    TimeoutSec = 20
    ContentType = 'application/json'
  }
  if ($null -ne $Body) {
    $params.Body = ($Body | ConvertTo-Json -Depth 20)
  }

  $response = Invoke-RestMethod @params
  if ($response.code -ne 0) {
    throw "Request failed: $Path => $($response.msg)"
  }
  return $response.data
}

function New-ApproveNode {
  param(
    [string]$Id,
    [string]$Name,
    [string]$CandidateParam,
    [string]$ShowText,
    [object]$ChildNode = $null
  )

  $node = [ordered]@{
    id = $Id
    type = 11
    name = $Name
    showText = $ShowText
    candidateStrategy = 10
    candidateParam = $CandidateParam
    approveType = 1
    approveMethod = 4
    approveRatio = 100
    signEnable = $false
    reasonRequire = $true
    rejectHandler = @{
      type = 1
    }
    assignStartUserHandlerType = 1
    assignEmptyHandler = @{
      type = 3
      userIds = @(1)
    }
  }
  if ($ChildNode) {
    $node.childNode = $ChildNode
  }
  return $node
}

$endNode = @{
  id = 'purchaseOrderApproveEnd'
  type = 1
  name = 'End'
}

$managerNode = New-ApproveNode `
  -Id 'purchaseOrderApproveManager' `
  -Name 'Purchase Manager Approval' `
  -CandidateParam '920003' `
  -ShowText 'Assign to role: 采购审批经理' `
  -ChildNode $endNode

$leaderNode = New-ApproveNode `
  -Id 'purchaseOrderApproveLeader' `
  -Name 'Purchase Leader Approval' `
  -CandidateParam '920002' `
  -ShowText 'Assign to role: 采购审批组长' `
  -ChildNode $managerNode

$modelBody = [ordered]@{
  key = 'erp_purchase_order'
  name = 'ERP Purchase Order Approval'
  category = 'erp_approval'
  description = 'Default ERP purchase order approval workflow'
  type = 20
  formType = 20
  formCustomCreatePath = '/erp/purchase/order'
  formCustomViewPath = '/erp/purchase/order/bpm/detail/index'
  visible = $true
  managerUserIds = @(1)
  allowCancelRunningProcess = $true
  simpleModel = $leaderNode
}

$models = Invoke-YudaoJson -Method 'GET' -Path '/bpm/model/list?name=ERP%20Purchase%20Order%20Approval'
$model = $models | Where-Object { $_.key -eq 'erp_purchase_order' } | Select-Object -First 1

if ($null -eq $model) {
  Write-Output 'Creating BPM model: erp_purchase_order'
  $modelId = Invoke-YudaoJson -Method 'POST' -Path '/bpm/model/create' -Body $modelBody
} else {
  Write-Output "Updating BPM model: erp_purchase_order (id=$($model.id))"
  $modelBody.id = [string]$model.id
  Invoke-YudaoJson -Method 'PUT' -Path '/bpm/model/update' -Body $modelBody | Out-Null
  $modelId = [string]$model.id
}

Write-Output "Deploying BPM model: $modelId"
Invoke-YudaoJson -Method 'POST' -Path "/bpm/model/deploy?id=$modelId" | Out-Null

$installedModel = Invoke-YudaoJson -Method 'GET' -Path "/bpm/model/get?id=$modelId"
if (-not $installedModel.processDefinition -or -not $installedModel.processDefinition.id) {
  throw 'Deployment completed, but no process definition information was returned'
}

Write-Output 'Purchase order BPM installation completed'
Write-Output ("Model ID: " + $installedModel.id)
Write-Output ("Process Definition ID: " + $installedModel.processDefinition.id)
Write-Output ("Process Key: " + $installedModel.key)
Write-Output ("Version: " + $installedModel.processDefinition.version)
