package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerConfigDO;
import cn.weitee.erp.module.system.service.permission.PermissionService;
import cn.weitee.erp.module.system.service.permission.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceDataPermissionServiceImplTest {

    private static final Long USER_ID = 100L;
    private static final Long EXTERNAL_LEDGER_ID = 99603L;
    private static final Long INTERNAL_LEDGER_ID = 99604L;

    @Mock
    private ErpFinanceLedgerRoleService ledgerRoleService;
    @Mock
    private PermissionService permissionService;
    @Mock
    private RoleService roleService;
    @Mock
    private ErpFinanceDualLedgerConfigService dualLedgerConfigService;

    @InjectMocks
    private FinanceDataPermissionServiceImpl service;

    @Test
    void auditUser_shouldAccessExternalLedgerOnly() {
        givenAuditUser();
        givenEnabledDualLedgerConfigs();

        assertTrue(service.canAccessLedger(USER_ID, EXTERNAL_LEDGER_ID));
        assertFalse(service.canAccessLedger(USER_ID, INTERNAL_LEDGER_ID));
    }

    @Test
    void auditUser_shouldNotAccessDualLedgerResult() {
        givenAuditRole();
        assertFalse(service.canAccessDualLedger(USER_ID, 11));
    }

    @Test
    void regularUser_shouldNeedBothLedgersForDualLedgerResult() {
        givenRegularUser();
        when(ledgerRoleService.getVisibleLedgerIdsByRoleIds(List.of(10L)))
                .thenReturn(List.of(EXTERNAL_LEDGER_ID));
        givenEnabledDualLedgerConfig();

        assertFalse(service.canAccessDualLedger(USER_ID, 11));
    }

    @Test
    void regularUser_withBothLedgers_shouldAccessDualLedgerResult() {
        givenRegularUser();
        when(ledgerRoleService.getVisibleLedgerIdsByRoleIds(List.of(10L)))
                .thenReturn(List.of(EXTERNAL_LEDGER_ID, INTERNAL_LEDGER_ID));
        givenEnabledDualLedgerConfig();

        assertTrue(service.canAccessDualLedger(USER_ID, 11));
    }

    private void givenAuditUser() {
        givenAuditRole();
        when(permissionService.getUserRoleIdListByUserIdFromCache(USER_ID)).thenReturn(Set.of(10L));
        when(roleService.getRoleListFromCache(anyList())).thenReturn(List.of());
        when(ledgerRoleService.getVisibleLedgerIdsByRoleIds(List.of(10L)))
                .thenReturn(List.of(EXTERNAL_LEDGER_ID, INTERNAL_LEDGER_ID));
    }

    private void givenAuditRole() {
        when(permissionService.hasAnyRoles(USER_ID, "finance_audit")).thenReturn(true);
    }

    private void givenRegularUser() {
        when(permissionService.hasAnyRoles(USER_ID, "finance_audit")).thenReturn(false);
        when(permissionService.getUserRoleIdListByUserIdFromCache(USER_ID)).thenReturn(Set.of(10L));
        when(roleService.getRoleListFromCache(anyList())).thenReturn(List.of());
    }

    private void givenEnabledDualLedgerConfigs() {
        when(dualLedgerConfigService.getDualLedgerConfigListByStatus(anyInt()))
                .thenReturn(List.of(new ErpFinanceDualLedgerConfigDO()
                        .setBizType(11)
                        .setExternalLedgerId(EXTERNAL_LEDGER_ID)
                        .setInternalLedgerId(INTERNAL_LEDGER_ID)));
    }

    private void givenEnabledDualLedgerConfig() {
        when(dualLedgerConfigService.getEnabledDualLedgerConfig(anyInt()))
                .thenReturn(new ErpFinanceDualLedgerConfigDO()
                        .setBizType(11)
                        .setExternalLedgerId(EXTERNAL_LEDGER_ID)
                        .setInternalLedgerId(INTERNAL_LEDGER_ID));
    }

}
