package cn.weitee.erp.module.erp.controller.admin.purchase;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ErpPurchaseOrderControllerPermissionTest {

    @Test
    void submitAndCancelEndpoints_shouldUseDedicatedBpmPermissions() throws Exception {
        assertPreAuthorizeValue("updatePurchaseOrderStatus",
                "@ss.hasPermission('erp:purchase-order:update-status')");
        assertPreAuthorizeValue("submitPurchaseOrder",
                "@ss.hasPermission('erp:purchase-order:submit')");
        assertPreAuthorizeValue("cancelPurchaseOrderApproval",
                "@ss.hasPermission('erp:purchase-order:cancel-approval')");
    }

    private static void assertPreAuthorizeValue(String methodName, String expectedValue) throws Exception {
        Method method = findMethod(methodName);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, () -> methodName + " should declare @PreAuthorize");
        assertEquals(expectedValue, annotation.value());
    }

    private static Method findMethod(String methodName) {
        for (Method method : ErpPurchaseOrderController.class.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Method not found: " + methodName);
    }
}
