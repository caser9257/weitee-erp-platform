package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.weitee.erp.module.erp.service.finance.interceptor.FinanceDataPermission;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ErpFinanceLedgerControllerPermissionTest {

    @Test
    void ledgerReadEndpoints_shouldEstablishFinancePermissionContext() throws NoSuchMethodException {
        assertTrue(hasFinanceDataPermission("getFinanceLedger", Long.class));
        assertTrue(hasFinanceDataPermission("getFinanceLedgerSimpleList"));
        assertTrue(hasFinanceDataPermission("getFinanceLedgerPage", cn.weitee.erp.module.erp.controller.admin.finance.vo.ledger.ErpFinanceLedgerPageReqVO.class));
        assertTrue(hasFinanceDataPermission("exportFinanceLedgerExcel", cn.weitee.erp.module.erp.controller.admin.finance.vo.ledger.ErpFinanceLedgerPageReqVO.class,
                jakarta.servlet.http.HttpServletResponse.class));
    }

    private boolean hasFinanceDataPermission(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = ErpFinanceLedgerController.class.getDeclaredMethod(methodName, parameterTypes);
        return method.isAnnotationPresent(FinanceDataPermission.class);
    }

}
