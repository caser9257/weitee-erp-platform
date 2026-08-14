param(
    [Parameter(Mandatory)]
    [string] $Database,
    [string] $Container = 'weitee-verify2-mysql'
)

function Invoke-MySql {
    param([string] $Sql)

    $result = & wsl.exe -e docker exec $Container mysql -uroot -p123456 --batch --skip-column-names $Database -e $Sql
    if ($LASTEXITCODE -ne 0) {
        throw "MySQL query failed: $Sql"
    }
    return @($result | Where-Object { $_ -and $_ -notmatch '^mysql: \[Warning\]' })
}

$scmRoles = @(Invoke-MySql "SELECT GROUP_CONCAT(role_id ORDER BY role_id) FROM system_user_role ur JOIN system_users u ON u.id = ur.user_id WHERE u.username = 'scm01' AND u.deleted = b'0' AND ur.deleted = b'0';")
if ($scmRoles.Count -ne 1 -or $scmRoles[0] -ne '910004') {
    throw "scm01 roles must be 910004, got $($scmRoles -join ',')"
}

$forbiddenRoots = @(Invoke-MySql "SELECT DISTINCT root.path FROM system_users u JOIN system_user_role ur ON ur.user_id = u.id AND ur.deleted = b'0' JOIN system_role_menu rm ON rm.role_id = ur.role_id AND rm.deleted = b'0' JOIN system_menu root ON root.id = rm.menu_id AND root.parent_id = 0 AND root.deleted = b'0' WHERE u.username = 'scm01' AND u.deleted = b'0' AND root.path IN ('/rd', '/finance', '/system');")
if ($forbiddenRoots.Count -gt 0) {
    throw "scm01 exposes forbidden roots: $($forbiddenRoots -join ',')"
}

$assemblePermissions = @(Invoke-MySql "SELECT GROUP_CONCAT(DISTINCT menu.permission ORDER BY menu.permission SEPARATOR ',') FROM system_users u JOIN system_user_role ur ON ur.user_id = u.id AND ur.deleted = b'0' JOIN system_role_menu rm ON rm.role_id = ur.role_id AND rm.deleted = b'0' JOIN system_menu menu ON menu.id = rm.menu_id AND menu.deleted = b'0' WHERE u.username = 'scm01' AND u.deleted = b'0' AND menu.permission IN ('erp:stock-assemble:query', 'erp:stock-assemble:create', 'erp:stock-assemble:update', 'erp:stock-assemble:update-status', 'erp:stock-assemble:delete');")
$expectedAssemblePermissions = 'erp:stock-assemble:create,erp:stock-assemble:delete,erp:stock-assemble:query,erp:stock-assemble:update,erp:stock-assemble:update-status'
if ($assemblePermissions.Count -ne 1 -or $assemblePermissions[0] -ne $expectedAssemblePermissions) {
    throw "scm01 must have complete stock assemble permissions, got $($assemblePermissions -join ',')"
}

$bomPermissions = @(Invoke-MySql "SELECT GROUP_CONCAT(DISTINCT menu.permission ORDER BY menu.permission SEPARATOR ',') FROM system_users u JOIN system_user_role ur ON ur.user_id = u.id AND ur.deleted = b'0' JOIN system_role_menu rm ON rm.role_id = ur.role_id AND rm.deleted = b'0' JOIN system_menu menu ON menu.id = rm.menu_id AND menu.deleted = b'0' WHERE u.username = 'scm01' AND u.deleted = b'0' AND menu.permission IN ('erp:bom:query', 'erp:bom:create', 'erp:bom:update', 'erp:bom:delete');")
$expectedBomPermissions = 'erp:bom:create,erp:bom:delete,erp:bom:query,erp:bom:update'
if ($bomPermissions.Count -ne 1 -or $bomPermissions[0] -ne $expectedBomPermissions) {
    throw "scm01 must have complete BOM permissions, got $($bomPermissions -join ',')"
}

$workflowPermissions = @(Invoke-MySql "SELECT GROUP_CONCAT(DISTINCT menu.permission ORDER BY menu.permission SEPARATOR ',') FROM system_users u JOIN system_user_role ur ON ur.user_id = u.id AND ur.deleted = b'0' JOIN system_role_menu rm ON rm.role_id = ur.role_id AND rm.deleted = b'0' JOIN system_menu menu ON menu.id = rm.menu_id AND menu.deleted = b'0' WHERE u.username = 'scm01' AND u.deleted = b'0' AND menu.permission IN ('erp:stock-in:submit', 'erp:stock-in:cancel-approval', 'erp:stock-out:submit', 'erp:stock-out:cancel-approval', 'erp:purchase-in-quality:update');")
$expectedWorkflowPermissions = 'erp:purchase-in-quality:update,erp:stock-in:cancel-approval,erp:stock-in:submit,erp:stock-out:cancel-approval,erp:stock-out:submit'
if ($workflowPermissions.Count -ne 1 -or $workflowPermissions[0] -ne $expectedWorkflowPermissions) {
    throw "scm01 must have the stock BPM and IQC return permissions required by the experience workflows, got $($workflowPermissions -join ',')"
}

$financeVoucherUpdatePermissions = @(Invoke-MySql "SELECT GROUP_CONCAT(DISTINCT menu.permission ORDER BY menu.permission SEPARATOR ',') FROM system_users u JOIN system_user_role ur ON ur.user_id = u.id AND ur.deleted = b'0' JOIN system_role_menu rm ON rm.role_id = ur.role_id AND rm.deleted = b'0' JOIN system_menu menu ON menu.id = rm.menu_id AND menu.deleted = b'0' WHERE u.username = 'finance01' AND u.deleted = b'0' AND menu.permission IN ('erp:finance-voucher:query', 'erp:finance-voucher:update');")
$expectedFinanceVoucherUpdatePermissions = 'erp:finance-voucher:query,erp:finance-voucher:update'
if ($financeVoucherUpdatePermissions.Count -ne 1 -or $financeVoucherUpdatePermissions[0] -ne $expectedFinanceVoucherUpdatePermissions) {
    throw "finance01 must have voucher query and update permissions required by the finance acceptance flow, got $($financeVoucherUpdatePermissions -join ',')"
}

$missingVoucherPeriods = @(Invoke-MySql "WITH required_ledgers AS (SELECT id AS ledger_id FROM erp_finance_ledger WHERE default_status = b'1' AND status = 0 AND deleted = b'0' UNION SELECT external_ledger_id FROM erp_finance_dual_ledger_config WHERE status = 0 AND deleted = b'0' UNION SELECT internal_ledger_id FROM erp_finance_dual_ledger_config WHERE status = 0 AND deleted = b'0') SELECT CONCAT(ledger.id, ':', ledger.name) FROM required_ledgers required LEFT JOIN erp_finance_ledger ledger ON ledger.id = required.ledger_id AND ledger.deleted = b'0' LEFT JOIN erp_finance_period period ON period.ledger_id = required.ledger_id AND period.deleted = b'0' AND period.status = 10 AND CURDATE() BETWEEN period.start_date AND period.end_date WHERE period.id IS NULL ORDER BY ledger.id;")
if ($missingVoucherPeriods.Count -gt 0) {
    throw "auto-voucher ledgers must have an open current period, missing $($missingVoucherPeriods -join ',')"
}

Write-Output 'PASS demo account least privilege'
