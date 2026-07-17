package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualWriteLogDO;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceDualProductCostService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceDualProjectCostService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceDualWriteService;
import cn.weitee.erp.module.erp.service.finance.FinanceDataPermissionService;
import cn.weitee.erp.module.erp.convert.finance.ErpFinanceDualWriteConvert;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualwrite.ErpFinanceDualWriteLogPageReqVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static cn.weitee.erp.framework.common.exception.enums.GlobalErrorCodeConstants.NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErpFinanceInternalLedgerVisibilityControllerTest {

    private static final Integer FINANCE_EXPENSE = 40;

    @Mock
    private FinanceDataPermissionService financeDataPermissionService;
    @Mock
    private ErpFinanceDualWriteService dualWriteService;
    @Mock
    private ErpFinanceDualWriteConvert dualWriteConvert;
    @Mock
    private ErpFinanceDualProductCostService productCostService;
    @Mock
    private ErpFinanceDualProjectCostService projectCostService;
    @InjectMocks
    private ErpFinanceDualWriteController dualWriteController;
    @InjectMocks
    private ErpFinanceDualProductCostController productCostController;
    @InjectMocks
    private ErpFinanceDualProjectCostController projectCostController;

    @Test
    void dualWriteLogGet_whenTargetLedgerIsHidden_returnsNull() {
        ErpFinanceDualWriteLogDO log = new ErpFinanceDualWriteLogDO()
                .setSourceLedgerId(99603L).setTargetLedgerId(99604L);
        when(dualWriteService.getDualWriteLog(1L)).thenReturn(log);
        when(financeDataPermissionService.canAccessLedger(99603L)).thenReturn(true);
        when(financeDataPermissionService.canAccessLedger(99604L)).thenReturn(false);

        assertNull(dualWriteController.getDualWriteLog(1L).getData());
        verify(dualWriteConvert, never()).convert(log);
    }

    @Test
    void dualWriteLogPage_whenTargetLedgerIsHidden_returnsEmptyPage() {
        ErpFinanceDualWriteLogDO log = new ErpFinanceDualWriteLogDO()
                .setSourceLedgerId(99603L).setTargetLedgerId(99604L);
        when(dualWriteService.getDualWriteLogPage(any(ErpFinanceDualWriteLogPageReqVO.class)))
                .thenReturn(new PageResult<>(java.util.List.of(log), 1L));
        when(financeDataPermissionService.canAccessLedger(99603L)).thenReturn(true);
        when(financeDataPermissionService.canAccessLedger(99604L)).thenReturn(false);

        PageResult<?> result = dualWriteController.getDualWriteLogPage(new ErpFinanceDualWriteLogPageReqVO()).getData();

        assertEquals(0L, result.getTotal());
        assertTrue(result.getList().isEmpty());
        verify(dualWriteConvert, never()).convertPage(any());
    }

    @Test
    void dualWriteRetry_whenTargetLedgerIsHidden_returnsNotFound() {
        ErpFinanceDualWriteLogDO log = new ErpFinanceDualWriteLogDO()
                .setSourceLedgerId(99603L).setTargetLedgerId(99604L);
        when(dualWriteService.getDualWriteLog(1L)).thenReturn(log);
        when(financeDataPermissionService.canAccessLedger(99603L)).thenReturn(true);
        when(financeDataPermissionService.canAccessLedger(99604L)).thenReturn(false);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> dualWriteController.retryDualWrite(1L));

        assertEquals(NOT_FOUND.getCode(), exception.getCode());
        verify(dualWriteService, never()).retryDualWrite(1L);
    }

    @Test
    void productDualCostPage_whenDualLedgerIsHidden_returnsEmptyPage() {
        when(financeDataPermissionService.canAccessDualLedger(null, 32)).thenReturn(false);

        PageResult<?> result = productCostController.getProductDualCostPage(
                new cn.weitee.erp.module.erp.controller.admin.finance.vo.productdualcost.ErpFinanceDualProductCostPageReqVO())
                .getData();

        assertEquals(0L, result.getTotal());
        verifyNoInteractions(productCostService);
    }

    @Test
    void projectDualCostRead_whenDualLedgerIsHidden_returnsNull() {
        when(financeDataPermissionService.canAccessDualLedger(null, FINANCE_EXPENSE)).thenReturn(false);

        assertNull(projectCostController.getProjectDualCost(1L).getData());
        verifyNoInteractions(projectCostService);
    }

    @Test
    void projectDualCostRebuild_whenDualLedgerIsHidden_returnsNotFound() {
        when(financeDataPermissionService.canAccessDualLedger(null, FINANCE_EXPENSE)).thenReturn(false);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> projectCostController.rebuildBatchByPeriod("2026-05", null));

        assertEquals(NOT_FOUND.getCode(), exception.getCode());
        verifyNoInteractions(projectCostService);
    }
}
