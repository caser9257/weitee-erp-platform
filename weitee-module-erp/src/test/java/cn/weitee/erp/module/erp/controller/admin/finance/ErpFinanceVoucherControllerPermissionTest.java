package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherDO;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceLedgerService;
import cn.weitee.erp.module.erp.service.finance.ErpFinancePeriodService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceVoucherService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceVoucherTemplateService;
import cn.weitee.erp.module.erp.service.finance.FinanceDataPermissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErpFinanceVoucherControllerPermissionTest {

    @Mock
    private ErpFinanceVoucherService financeVoucherService;
    @Mock
    private FinanceDataPermissionService financeDataPermissionService;
    @Mock
    private ErpFinanceLedgerService financeLedgerService;
    @Mock
    private ErpFinancePeriodService financePeriodService;
    @Mock
    private ErpFinanceVoucherTemplateService voucherTemplateService;
    @InjectMocks
    private ErpFinanceVoucherController controller;

    @Test
    void getVoucherByBiz_whenLedgerIsVisible_returnsQueryResult() {
        lenient().when(financeDataPermissionService.canAccessLedger(99603L)).thenReturn(true);

        CommonResult<ErpFinanceVoucherRespVO> result = controller.getVoucherByBiz(99603L, 11, 1L);

        assertNull(result.getData());
        verify(financeVoucherService).getVoucherByLedgerAndBiz(99603L, 11, 1L);
    }

    @Test
    void getVoucherByBiz_whenLedgerIsHidden_throwsForbiddenBeforeQuery() {
        when(financeDataPermissionService.canAccessLedger(99604L)).thenReturn(false);

        assertThrows(ServiceException.class, () -> controller.getVoucherByBiz(99604L, 11, 1L));

        verifyNoInteractions(financeVoucherService);
    }

    @Test
    void getVoucher_whenVoucherBelongsToHiddenLedger_throwsForbidden() {
        when(financeVoucherService.getVoucher(100L)).thenReturn(new ErpFinanceVoucherDO().setLedgerId(99604L));
        when(financeDataPermissionService.canAccessLedger(99604L)).thenReturn(false);

        assertThrows(ServiceException.class, () -> controller.getVoucher(100L));
    }
}
