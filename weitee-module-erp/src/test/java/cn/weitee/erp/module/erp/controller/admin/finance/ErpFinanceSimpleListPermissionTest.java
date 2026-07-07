package cn.weitee.erp.module.erp.controller.admin.finance;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ErpFinanceSimpleListPermissionTest {

    @Test
    void simpleListEndpoints_shouldDeclareExplicitQueryPermissions() throws Exception {
        assertPreAuthorizeValue(ErpFinanceDualLedgerConfigController.class, "getDualLedgerConfigSimpleList",
                "@ss.hasPermission('erp:finance-dual-ledger-config:query')");
        assertPreAuthorizeValue(ErpFinanceLedgerController.class, "getFinanceLedgerSimpleList",
                "@ss.hasPermission('erp:finance-ledger:query')");
    }

    private static void assertPreAuthorizeValue(Class<?> controllerClass, String methodName,
                                                String expectedValue) throws Exception {
        Method method = findMethod(controllerClass, methodName);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, () -> methodName + " should declare @PreAuthorize");
        assertEquals(expectedValue, annotation.value());
    }

    private static Method findMethod(Class<?> controllerClass, String methodName) {
        for (Method method : controllerClass.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Method not found: " + controllerClass.getSimpleName() + "." + methodName);
    }
}
