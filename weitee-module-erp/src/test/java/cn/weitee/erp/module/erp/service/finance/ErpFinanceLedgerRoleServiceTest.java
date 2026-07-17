package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerRoleDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceRoleDeptDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceRoleSubjectDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceLedgerRoleMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceRoleDeptMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceRoleSubjectMapper;
import cn.weitee.erp.module.system.service.permission.PermissionService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 账簿角色关联服务单元测试
 */
class ErpFinanceLedgerRoleServiceTest {

    @Test
    void createLedgerRole_shouldInsertRecord() throws Exception {
        ErpFinanceLedgerRoleServiceImpl service = new ErpFinanceLedgerRoleServiceImpl();
        AtomicReference<ErpFinanceLedgerRoleDO> insertedRef = new AtomicReference<>();

        setField(service, "ledgerRoleMapper", createProxy(ErpFinanceLedgerRoleMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                ErpFinanceLedgerRoleDO record = (ErpFinanceLedgerRoleDO) args[0];
                record.setId(1L);
                insertedRef.set(record);
                return 1;
            }
            return null;
        }));

        Long id = service.createLedgerRole(100L, 200L);

        assertEquals(1L, id);
        assertNotNull(insertedRef.get());
        assertEquals(100L, insertedRef.get().getLedgerId());
        assertEquals(200L, insertedRef.get().getRoleId());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), insertedRef.get().getStatus());
    }

    @Test
    void getVisibleLedgerIdsByRoleIds_shouldReturnLedgerIds() throws Exception {
        ErpFinanceLedgerRoleServiceImpl service = new ErpFinanceLedgerRoleServiceImpl();

        setField(service, "ledgerRoleMapper", createProxy(ErpFinanceLedgerRoleMapper.class, (methodName, args) -> {
            if ("selectLedgerIdsByRoleIds".equals(methodName)) {
                return Arrays.asList(100L, 200L, 300L);
            }
            return null;
        }));

        List<Long> result = service.getVisibleLedgerIdsByRoleIds(Arrays.asList(1L, 2L));

        assertEquals(3, result.size());
        assertTrue(result.contains(100L));
        assertTrue(result.contains(200L));
        assertTrue(result.contains(300L));
    }

    @Test
    void getVisibleLedgerIdsByRoleIds_shouldReturnEmptyForEmptyInput() throws Exception {
        ErpFinanceLedgerRoleServiceImpl service = new ErpFinanceLedgerRoleServiceImpl();

        List<Long> result = service.getVisibleLedgerIdsByRoleIds(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void setLedgerRoles_shouldReplaceExistingRoles() throws Exception {
        ErpFinanceLedgerRoleServiceImpl service = new ErpFinanceLedgerRoleServiceImpl();
        AtomicReference<Long> deletedLedgerId = new AtomicReference<>();

        setField(service, "ledgerRoleMapper", createProxy(ErpFinanceLedgerRoleMapper.class, (methodName, args) -> {
            if ("delete".equals(methodName)) {
                // 记录删除的条件
                return 1;
            }
            if ("insert".equals(methodName)) {
                ErpFinanceLedgerRoleDO record = (ErpFinanceLedgerRoleDO) args[0];
                record.setId(1L);
                return 1;
            }
            return null;
        }));

        // 不应抛出异常
        assertDoesNotThrow(() -> service.setLedgerRoles(100L, Arrays.asList(1L, 2L, 3L)));
    }

    @Test
    void setLedgerRoles_shouldEvictUsersOfRemovedRoles() throws Exception {
        ErpFinanceLedgerRoleServiceImpl service = new ErpFinanceLedgerRoleServiceImpl();
        setField(service, "ledgerRoleMapper", createProxy(ErpFinanceLedgerRoleMapper.class, (methodName, args) -> {
            if ("selectRoleIdsByLedgerId".equals(methodName)) {
                return List.of(10L);
            }
            if ("delete".equals(methodName) || "insert".equals(methodName)) {
                return 1;
            }
            return null;
        }));
        PermissionService permissionService = mock(PermissionService.class);
        org.mockito.Mockito.when(permissionService.getUserRoleIdListByRoleId(org.mockito.ArgumentMatchers.anyCollection()))
                .thenReturn(Set.of(100L));
        setField(service, "permissionService", permissionService);
        UserLedgerPermissionCacheService cacheService = mock(UserLedgerPermissionCacheService.class);
        setField(service, "userLedgerPermissionCacheService", cacheService);

        service.setLedgerRoles(100L, List.of(20L));

        verify(cacheService).clearCache(100L);
    }

    @Test
    void clearUserLedgerPermissionCache_shouldEvictSpecifiedUserCache() throws Exception {
        ErpFinanceLedgerRoleServiceImpl service = new ErpFinanceLedgerRoleServiceImpl();
        UserLedgerPermissionCacheService cacheService = mock(UserLedgerPermissionCacheService.class);
        setField(service, "userLedgerPermissionCacheService", cacheService);

        service.clearUserLedgerPermissionCache(100L);

        verify(cacheService).clearCache(100L);
    }

    @Test
    void clearUserLedgerPermissionCache_shouldIgnoreNullUserId() throws Exception {
        ErpFinanceLedgerRoleServiceImpl service = new ErpFinanceLedgerRoleServiceImpl();
        UserLedgerPermissionCacheService cacheService = mock(UserLedgerPermissionCacheService.class);
        setField(service, "userLedgerPermissionCacheService", cacheService);

        service.clearUserLedgerPermissionCache(null);

        verify(cacheService, never()).clearCache(null);
    }

    @Test
    void setRoleDeptIds_shouldReplaceMappingsAndEvictAffectedUserCaches() throws Exception {
        ErpFinanceLedgerRoleServiceImpl service = new ErpFinanceLedgerRoleServiceImpl();
        AtomicReference<List<ErpFinanceRoleDeptDO>> insertedRef = new AtomicReference<>();
        setField(service, "roleDeptMapper", createProxy(ErpFinanceRoleDeptMapper.class, (methodName, args) -> {
            if ("delete".equals(methodName)) {
                return 1;
            }
            if ("insertBatch".equals(methodName)) {
                insertedRef.set((List<ErpFinanceRoleDeptDO>) args[0]);
                return true;
            }
            return null;
        }));
        PermissionService permissionService = mock(PermissionService.class);
        org.mockito.Mockito.when(permissionService.getUserRoleIdListByRoleId(org.mockito.ArgumentMatchers.anyCollection()))
                .thenReturn(Set.of(100L));
        setField(service, "permissionService", permissionService);
        UserLedgerPermissionCacheService cacheService = mock(UserLedgerPermissionCacheService.class);
        setField(service, "userLedgerPermissionCacheService", cacheService);

        service.setRoleDeptIds(200L, List.of(10L, 20L));

        assertEquals(2, insertedRef.get().size());
        assertTrue(insertedRef.get().stream().allMatch(item -> item.getRoleId().equals(200L)));
        verify(cacheService).clearCache(100L);
    }

    @Test
    void setRoleSubjectCodes_shouldReplaceMappingsAndEvictAffectedUserCaches() throws Exception {
        ErpFinanceLedgerRoleServiceImpl service = new ErpFinanceLedgerRoleServiceImpl();
        AtomicReference<List<ErpFinanceRoleSubjectDO>> insertedRef = new AtomicReference<>();
        setField(service, "roleSubjectMapper", createProxy(ErpFinanceRoleSubjectMapper.class, (methodName, args) -> {
            if ("delete".equals(methodName)) {
                return 1;
            }
            if ("insertBatch".equals(methodName)) {
                insertedRef.set((List<ErpFinanceRoleSubjectDO>) args[0]);
                return true;
            }
            return null;
        }));
        PermissionService permissionService = mock(PermissionService.class);
        org.mockito.Mockito.when(permissionService.getUserRoleIdListByRoleId(org.mockito.ArgumentMatchers.anyCollection()))
                .thenReturn(Set.of(100L));
        setField(service, "permissionService", permissionService);
        UserLedgerPermissionCacheService cacheService = mock(UserLedgerPermissionCacheService.class);
        setField(service, "userLedgerPermissionCacheService", cacheService);

        service.setRoleSubjectCodes(200L, 99603L, List.of("1001", "1001", "6601"));

        assertEquals(2, insertedRef.get().size());
        assertTrue(insertedRef.get().stream().allMatch(item -> item.getRoleId().equals(200L)));
        assertTrue(insertedRef.get().stream().allMatch(item -> item.getLedgerId().equals(99603L)));
        verify(cacheService).clearCache(100L);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @SuppressWarnings("unchecked")
    private static <T> T createProxy(Class<T> interfaceType, java.util.function.BiFunction<String, Object[], Object> handler) {
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{interfaceType},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        if ("toString".equals(method.getName())) {
                            return interfaceType.getSimpleName() + "Proxy";
                        }
                        if ("hashCode".equals(method.getName())) {
                            return System.identityHashCode(proxy);
                        }
                        if ("equals".equals(method.getName())) {
                            return proxy == args[0];
                        }
                    }
                    return handler.apply(method.getName(), args);
                }
        );
    }
}
