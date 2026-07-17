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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static cn.weitee.erp.framework.common.exception.enums.GlobalErrorCodeConstants.NOT_FOUND;

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
    void getDualLedgerResult_whenUserLacksOneLedger_returnsNullWithoutQuery() {
        when(financeDataPermissionService.canAccessDualLedger(null, 11)).thenReturn(false);

        assertNull(controller.getDualLedgerResult(11, 99L).getData());

        verifyNoInteractions(dualLedgerResultService);
    }

    @Test
    void getDualLedgerResultPage_whenUserLacksOneLedger_returnsEmptyPageWithoutQuery() {
        when(financeDataPermissionService.canAccessDualLedger(null, 11)).thenReturn(false);

        var result = controller.getDualLedgerResultPage(
                new cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerResultPageReqVO()
                        .setBizType(11));

        assertEquals(0L, result.getData().getTotal());
        assertEquals(0, result.getData().getList().size());
        verifyNoInteractions(dualLedgerResultService);
    }

    @Test
    void recomputeByBiz_whenUserLacksOneLedger_returnsNotFoundWithoutWrite() {
        when(financeDataPermissionService.canAccessDualLedger(null, 11)).thenReturn(false);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> controller.recomputeByBizId(11, 99L));

        assertEquals(NOT_FOUND.getCode(), exception.getCode());
        verifyNoInteractions(dualLedgerResultService, dualWriteService);
    }
}
