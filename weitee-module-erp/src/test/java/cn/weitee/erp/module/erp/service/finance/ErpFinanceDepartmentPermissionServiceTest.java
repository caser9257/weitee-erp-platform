package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetDepreciationPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpensePageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceAssetDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceAssetDepreciationDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceAssetMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceAssetDepreciationMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceExpenseMapper;
import cn.weitee.erp.module.erp.service.finance.interceptor.FinancePermissionScope;
import cn.weitee.erp.module.erp.service.finance.interceptor.FinanceDataPermissionContext;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static cn.weitee.erp.framework.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;

class ErpFinanceDepartmentPermissionServiceTest {

    @Test
    void getFinanceExpensePage_whenDeptScopeLimited_shouldUseDeptScopedMapper() throws Exception {
        ErpFinanceExpenseServiceImpl service = new ErpFinanceExpenseServiceImpl();
        AtomicReference<Collection<Long>> actualDeptIds = new AtomicReference<>();
        setField(service, "financeDataPermissionService", permissionService(FinancePermissionScope.Scope.limited(Set.of(7L))));
        setField(service, "erpFinanceExpenseMapper", createProxy(ErpFinanceExpenseMapper.class, (methodName, args) -> {
            if ("selectPageByDeptIds".equals(methodName)) {
                actualDeptIds.set((Collection<Long>) args[1]);
                return new PageResult<>(List.of(new ErpFinanceExpenseDO().setId(101L).setDeptId(7L)), 1L);
            }
            if ("selectPage".equals(methodName)) {
                throw new AssertionError("受限部门不得调用全量费用分页查询");
            }
            return null;
        }));

        PageResult<ErpFinanceExpenseDO> page = service.getFinanceExpensePage(new ErpFinanceExpensePageReqVO());

        assertEquals(Set.of(7L), Set.copyOf(actualDeptIds.get()));
        assertEquals(List.of(101L), page.getList().stream().map(ErpFinanceExpenseDO::getId).toList());
    }

    @Test
    void getFinanceAssetPage_whenDeptScopeLimited_shouldUseDeptScopedMapper() throws Exception {
        ErpFinanceAssetServiceImpl service = new ErpFinanceAssetServiceImpl();
        AtomicReference<Collection<Long>> actualDeptIds = new AtomicReference<>();
        setField(service, "financeDataPermissionService", permissionService(FinancePermissionScope.Scope.limited(Set.of(8L))));
        setField(service, "financeAssetMapper", createProxy(ErpFinanceAssetMapper.class, (methodName, args) -> {
            if ("selectPageByDeptIds".equals(methodName)) {
                actualDeptIds.set((Collection<Long>) args[1]);
                return new PageResult<>(List.of(new ErpFinanceAssetDO().setId(102L).setDeptId(8L)), 1L);
            }
            if ("selectPage".equals(methodName)) {
                throw new AssertionError("受限部门不得调用全量资产分页查询");
            }
            return null;
        }));

        PageResult<ErpFinanceAssetDO> page = service.getFinanceAssetPage(new ErpFinanceAssetPageReqVO());

        assertEquals(Set.of(8L), Set.copyOf(actualDeptIds.get()));
        assertEquals(List.of(102L), page.getList().stream().map(ErpFinanceAssetDO::getId).toList());
    }

    @Test
    void getFinanceExpense_whenContextDeptScopeExcludesExpense_shouldReject() throws Exception {
        ErpFinanceExpenseServiceImpl service = new ErpFinanceExpenseServiceImpl();
        setField(service, "erpFinanceExpenseMapper", createProxy(ErpFinanceExpenseMapper.class, (methodName, args) ->
                "selectById".equals(methodName) ? new ErpFinanceExpenseDO().setId(103L).setDeptId(10L) : null));
        FinanceDataPermissionContext.setPermissionScope(new FinancePermissionScope(
                FinancePermissionScope.Scope.all(), FinancePermissionScope.Scope.limited(Set.of(11L)),
                java.util.Collections.emptyMap(), false, false));

        try {
            ServiceException ex = assertThrows(ServiceException.class, () -> service.getFinanceExpense(103L));
            assertEquals(FORBIDDEN.getCode(), ex.getCode());
        } finally {
            FinanceDataPermissionContext.clear();
        }
    }

    @Test
    void getFinanceAsset_whenContextDeptScopeExcludesAsset_shouldReject() throws Exception {
        ErpFinanceAssetServiceImpl service = new ErpFinanceAssetServiceImpl();
        setField(service, "financeAssetMapper", createProxy(ErpFinanceAssetMapper.class, (methodName, args) ->
                "selectById".equals(methodName) ? new ErpFinanceAssetDO().setId(104L).setDeptId(12L) : null));
        FinanceDataPermissionContext.setPermissionScope(new FinancePermissionScope(
                FinancePermissionScope.Scope.all(), FinancePermissionScope.Scope.limited(Set.of(13L)),
                java.util.Collections.emptyMap(), false, false));

        try {
            ServiceException ex = assertThrows(ServiceException.class, () -> service.getFinanceAsset(104L));
            assertEquals(FORBIDDEN.getCode(), ex.getCode());
        } finally {
            FinanceDataPermissionContext.clear();
        }
    }

    @Test
    void getFinanceAssetDepreciationPage_whenDeptScopeLimited_shouldUseAuthorizedAssetIds() throws Exception {
        ErpFinanceAssetDepreciationServiceImpl service = new ErpFinanceAssetDepreciationServiceImpl();
        AtomicReference<Collection<Long>> actualAssetIds = new AtomicReference<>();
        setField(service, "financeDataPermissionService", permissionService(FinancePermissionScope.Scope.limited(Set.of(14L))));
        setField(service, "financeAssetMapper", createProxy(ErpFinanceAssetMapper.class, (methodName, args) ->
                "selectList".equals(methodName) ? List.of(new ErpFinanceAssetDO().setId(105L).setDeptId(14L)) : null));
        setField(service, "financeAssetDepreciationMapper", createProxy(ErpFinanceAssetDepreciationMapper.class, (methodName, args) -> {
            if ("selectPageByAssetIds".equals(methodName)) {
                actualAssetIds.set((Collection<Long>) args[1]);
                return new PageResult<>(List.of(new ErpFinanceAssetDepreciationDO().setId(106L).setAssetId(105L)), 1L);
            }
            if ("selectPage".equals(methodName)) {
                throw new AssertionError("受限部门不得调用全量折旧分页查询");
            }
            return null;
        }));

        PageResult<ErpFinanceAssetDepreciationDO> page = service.getFinanceAssetDepreciationPage(new ErpFinanceAssetDepreciationPageReqVO());

        assertEquals(Set.of(105L), Set.copyOf(actualAssetIds.get()));
        assertEquals(List.of(106L), page.getList().stream().map(ErpFinanceAssetDepreciationDO::getId).toList());
    }

    private FinanceDataPermissionService permissionService(FinancePermissionScope.Scope<Long> deptScope) {
        return createProxy(FinanceDataPermissionService.class, (methodName, args) -> {
            if ("getPermissionScope".equals(methodName)) {
                return new FinancePermissionScope(FinancePermissionScope.Scope.all(), deptScope, java.util.Collections.emptyMap(), false, false);
            }
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> type, MethodHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> handler.handle(method.getName(), args));
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }
}
