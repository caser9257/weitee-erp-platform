package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerConfigDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceRoleDeptDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceRoleSubjectDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceRoleDeptMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceRoleSubjectMapper;
import cn.weitee.erp.module.erp.service.finance.interceptor.FinancePermissionScope;
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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyCollection;
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
    @Mock
    private ErpFinanceRoleDeptMapper roleDeptMapper;
    @Mock
    private ErpFinanceRoleSubjectMapper roleSubjectMapper;

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

    @Test
    void regularUser_withoutLedgerMapping_shouldResolveNoneLedgerScope() throws Exception {
        givenRegularUser();
        when(ledgerRoleService.getVisibleLedgerIdsByRoleIds(List.of(10L))).thenReturn(List.of());

        java.lang.reflect.Method getPermissionScope = assertDoesNotThrow(
                () -> FinanceDataPermissionServiceImpl.class.getMethod("getPermissionScope", Long.class));
        Object scope = getPermissionScope.invoke(service, USER_ID);
        Object ledgerScope = scope.getClass().getMethod("ledgerScope").invoke(scope);
        Object mode = ledgerScope.getClass().getMethod("mode").invoke(ledgerScope);

        assertEquals("NONE", ((Enum<?>) mode).name());
    }

    @Test
    void regularUser_withDeptMapping_shouldOnlyAccessMappedDept() {
        givenRegularUser();
        when(roleDeptMapper.selectListByRoleIds(List.of(10L)))
                .thenReturn(List.of(new ErpFinanceRoleDeptDO().setRoleId(10L).setDeptId(20L)));

        FinancePermissionScope.Scope<Long> deptScope = service.getPermissionScope(USER_ID).deptScope();
        assertTrue(deptScope.values().contains(20L));
        assertFalse(deptScope.values().contains(21L));
    }

    @Test
    void regularUser_withSubjectMapping_shouldOnlyAccessMappedSubjectInVisibleLedger() {
        givenRegularUser();
        when(ledgerRoleService.getVisibleLedgerIdsByRoleIds(List.of(10L))).thenReturn(List.of(EXTERNAL_LEDGER_ID));
        when(roleSubjectMapper.selectListByRoleIds(List.of(10L))).thenReturn(List.of(
                new ErpFinanceRoleSubjectDO().setRoleId(10L).setLedgerId(EXTERNAL_LEDGER_ID).setSubjectCode("1001")));

        FinancePermissionScope permissionScope = service.getPermissionScope(USER_ID);
        assertTrue(permissionScope.subjectScopesByLedger().get(EXTERNAL_LEDGER_ID).values().contains("1001"));
        assertFalse(permissionScope.subjectScopesByLedger().get(EXTERNAL_LEDGER_ID).values().contains("6601"));
        assertFalse(permissionScope.subjectScopesByLedger().containsKey(INTERNAL_LEDGER_ID));
    }

    @Test
    void regularUser_withMultipleRoles_shouldUnionDeptAndSubjectScopes() {
        givenRegularUser();
        when(permissionService.getUserRoleIdListByUserIdFromCache(USER_ID)).thenReturn(Set.of(10L, 20L));
        when(ledgerRoleService.getVisibleLedgerIdsByRoleIds(anyList()))
                .thenReturn(List.of(EXTERNAL_LEDGER_ID, INTERNAL_LEDGER_ID));
        when(roleDeptMapper.selectListByRoleIds(anyCollection())).thenReturn(List.of(
                new ErpFinanceRoleDeptDO().setRoleId(10L).setDeptId(20L),
                new ErpFinanceRoleDeptDO().setRoleId(20L).setDeptId(21L)));
        when(roleSubjectMapper.selectListByRoleIds(anyCollection())).thenReturn(List.of(
                new ErpFinanceRoleSubjectDO().setRoleId(10L).setLedgerId(EXTERNAL_LEDGER_ID).setSubjectCode("1001"),
                new ErpFinanceRoleSubjectDO().setRoleId(20L).setLedgerId(EXTERNAL_LEDGER_ID).setSubjectCode("6601"),
                new ErpFinanceRoleSubjectDO().setRoleId(20L).setLedgerId(INTERNAL_LEDGER_ID).setSubjectCode("2202")));

        FinancePermissionScope permissionScope = service.getPermissionScope(USER_ID);

        assertEquals(Set.of(20L, 21L), permissionScope.deptScope().values());
        assertEquals(Set.of("1001", "6601"),
                permissionScope.subjectScopesByLedger().get(EXTERNAL_LEDGER_ID).values());
        assertEquals(Set.of("2202"),
                permissionScope.subjectScopesByLedger().get(INTERNAL_LEDGER_ID).values());
    }

    @Test
    void regularUser_withoutDeptOrSubjectMapping_shouldResolveNoneScopes() {
        givenRegularUser();
        when(ledgerRoleService.getVisibleLedgerIdsByRoleIds(List.of(10L))).thenReturn(List.of(EXTERNAL_LEDGER_ID));

        FinancePermissionScope permissionScope = service.getPermissionScope(USER_ID);

        assertEquals(FinancePermissionScope.ScopeMode.NONE, permissionScope.deptScope().mode());
        assertEquals(FinancePermissionScope.ScopeMode.NONE,
                permissionScope.subjectScopesByLedger().get(EXTERNAL_LEDGER_ID).mode());
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
