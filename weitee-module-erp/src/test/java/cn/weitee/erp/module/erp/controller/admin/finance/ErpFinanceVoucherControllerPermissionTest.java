package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherGenerateReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherActionReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherEntryDO;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static cn.weitee.erp.framework.common.exception.enums.GlobalErrorCodeConstants.NOT_FOUND;

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
    void getVoucherByBiz_whenLedgerIsHidden_returnsNullWithoutQuery() {
        when(financeDataPermissionService.canAccessLedger(99604L)).thenReturn(false);

        assertNull(controller.getVoucherByBiz(99604L, 11, 1L).getData());

        verifyNoInteractions(financeVoucherService);
    }

    @Test
    void getVoucher_whenVoucherBelongsToHiddenLedger_returnsNull() {
        when(financeVoucherService.getVoucher(100L)).thenReturn(new ErpFinanceVoucherDO().setLedgerId(99604L));
        when(financeDataPermissionService.canAccessLedger(99604L)).thenReturn(false);

        assertNull(controller.getVoucher(100L).getData());
    }

    @Test
    void getVoucher_whenVoucherBelongsToHiddenDept_returnsNull() {
        when(financeVoucherService.getVoucher(100L)).thenReturn(new ErpFinanceVoucherDO()
                .setId(100L).setLedgerId(99603L).setDeptId(200L));
        when(financeDataPermissionService.canAccessLedger(99603L)).thenReturn(true);
        when(financeDataPermissionService.canAccessDept(200L)).thenReturn(false);

        assertNull(controller.getVoucher(100L).getData());
    }

    @Test
    void getVoucher_whenAnyEntrySubjectIsHidden_returnsNull() {
        when(financeVoucherService.getVoucher(100L)).thenReturn(new ErpFinanceVoucherDO().setId(100L).setLedgerId(99603L));
        when(financeVoucherService.getVoucherEntryListByVoucherId(100L))
                .thenReturn(java.util.List.of(new ErpFinanceVoucherEntryDO().setSubjectCode("660201")));
        when(financeDataPermissionService.canAccessLedger(99603L)).thenReturn(true);
        when(financeDataPermissionService.canAccessSubject(99603L, "660201")).thenReturn(false);

        assertNull(controller.getVoucher(100L).getData());
    }

    @Test
    void generateVoucher_whenLedgerIsHidden_returnsNotFoundBeforeWrite() {
        when(financeDataPermissionService.canAccessLedger(99604L)).thenReturn(false);

        ServiceException exception = assertThrows(ServiceException.class, () -> controller.generateVoucher(
                new ErpFinanceVoucherGenerateReqVO().setLedgerId(99604L)));
        assertEquals(NOT_FOUND.getCode(), exception.getCode());

        verify(financeVoucherService, org.mockito.Mockito.never()).generateVoucher(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void approveVoucher_whenAnyVoucherBelongsToHiddenLedger_returnsNotFoundBeforeWrite() {
        when(financeVoucherService.getVoucherListByIds(java.util.List.of(100L)))
                .thenReturn(java.util.List.of(new ErpFinanceVoucherDO().setId(100L).setLedgerId(99604L)));
        when(financeDataPermissionService.canAccessLedger(99604L)).thenReturn(false);

        ServiceException exception = assertThrows(ServiceException.class, () -> controller.approveVoucher(
                new ErpFinanceVoucherActionReqVO().setIds(java.util.List.of(100L))));
        assertEquals(NOT_FOUND.getCode(), exception.getCode());

        verify(financeVoucherService, org.mockito.Mockito.never()).approveVoucher(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void approveVoucher_whenAnyEntrySubjectIsHidden_returnsNotFoundBeforeWrite() {
        when(financeVoucherService.getVoucherListByIds(java.util.List.of(100L)))
                .thenReturn(java.util.List.of(new ErpFinanceVoucherDO().setId(100L).setLedgerId(99603L)));
        when(financeVoucherService.getVoucherEntryListByVoucherId(100L))
                .thenReturn(java.util.List.of(new ErpFinanceVoucherEntryDO().setSubjectCode("660201")));
        when(financeDataPermissionService.canAccessLedger(99603L)).thenReturn(true);
        when(financeDataPermissionService.canAccessSubject(99603L, "660201")).thenReturn(false);

        ServiceException exception = assertThrows(ServiceException.class, () -> controller.approveVoucher(
                new ErpFinanceVoucherActionReqVO().setIds(java.util.List.of(100L))));
        assertEquals(NOT_FOUND.getCode(), exception.getCode());

        verify(financeVoucherService, org.mockito.Mockito.never()).approveVoucher(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}
