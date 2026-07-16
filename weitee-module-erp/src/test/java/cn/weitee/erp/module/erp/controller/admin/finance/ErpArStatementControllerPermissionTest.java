package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.weitee.erp.module.erp.controller.admin.finance.vo.arstatement.ErpArStatementRespVO;
import cn.weitee.erp.module.erp.service.finance.ErpArStatementService;
import cn.weitee.erp.module.erp.service.finance.interceptor.FinanceDataPermission;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ErpArStatementControllerPermissionTest {

    @Mock
    private ErpArStatementService statementService;
    @InjectMocks
    private ErpArStatementController controller;

    @Test
    void allReadEndpoints_shouldUseFinanceDataPermission() throws Exception {
        assertThat(hasPermission("getStatementPage")).isTrue();
        assertThat(hasPermission("getStatement")).isTrue();
        assertThat(hasPermission("getStatementSummary")).isTrue();
        assertThat(hasPermission("getStatementSummaryByOrderId")).isTrue();
        assertThat(hasPermission("exportArStatementExcel")).isTrue();
    }

    private boolean hasPermission(String methodName) {
        for (Method method : ErpArStatementController.class.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method.isAnnotationPresent(FinanceDataPermission.class);
            }
        }
        return false;
    }
}
