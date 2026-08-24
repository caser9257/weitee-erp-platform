package cn.weitee.erp.module.erp.service.finance.guard;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.service.finance.FinanceDataPermissionService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExternalLedgerWriteGuardTest {

    @Test
    void around_whenAuditRole_throwsForbiddenBeforeProceeding() throws Throwable {
        ExternalLedgerWriteGuard guard = new ExternalLedgerWriteGuard();
        FinanceDataPermissionService permissionService = mock(FinanceDataPermissionService.class);
        when(permissionService.isAuditRole()).thenReturn(true);
        setField(guard, "financeDataPermissionService", permissionService);
        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);

        assertThrows(ServiceException.class, () -> guard.around(point));

        verify(permissionService).isAuditRole();
        verify(point, never()).proceed();
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
