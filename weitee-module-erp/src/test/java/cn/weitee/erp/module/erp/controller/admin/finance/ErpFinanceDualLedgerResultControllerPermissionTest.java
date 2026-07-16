package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceDualLedgerResultService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceDualWriteService;
import cn.weitee.erp.module.erp.service.finance.FinanceDataPermissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErpFinanceDualLedgerResultControllerPermissionTest {

    @Mock
    private ErpFinanceDualLedgerResultService dualLedgerResultService;
    @Mock
    private ErpFinanceDualWriteService dualWriteService;
    @Mock
    private FinanceDataPermissionService financeDataPermissionService;
    @InjectMocks
    private ErpFinanceDualLedgerResultController controller;

    @Test
    void getDualLedgerResult_whenUserLacksOneLedger_throwsForbiddenBeforeQuery() {
        when(financeDataPermissionService.canAccessDualLedger(null, 11)).thenReturn(false);

        assertThrows(ServiceException.class, () -> controller.getDualLedgerResult(11, 99L));

        verifyNoInteractions(dualLedgerResultService);
    }
}
