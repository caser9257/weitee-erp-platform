package cn.weitee.erp.module.erp.service.purchase;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePaymentAllocateMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePrepaymentAllocateMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseReturnMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpFinancePaymentAllocateStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpFinancePrepaymentAllocateStatusEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_RETURN_PROCESS_FAIL_EXISTS_REFUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * P2 预备：采购入库/退货反审核守卫测试。
 * 守卫必须同时覆盖付款核销与预付款核销，防止预付款已核销的来源单据被反审核后
 * 台账关闭、核销事实悬挂（半条 AP 事实）。
 */
@ExtendWith(MockitoExtension.class)
class ErpPurchaseAllocateGuardTest {

    @InjectMocks
    private ErpPurchaseInQueryHelper purchaseInQueryHelper;
    @InjectMocks
    private ErpPurchaseReturnServiceImpl purchaseReturnService;

    @Mock
    private ErpPurchaseReturnMapper erpPurchaseReturnMapper;
    @Mock
    private ErpFinancePaymentAllocateMapper erpFinancePaymentAllocateMapper;
    @Mock
    private ErpFinancePrepaymentAllocateMapper erpFinancePrepaymentAllocateMapper;

    @Test
    void purchaseInHasApprovedAllocate_shouldDetectPrepaymentAllocateOnly() {
        when(erpFinancePaymentAllocateMapper.selectCountByBizTypeAndBizIdAndStatus(
                ErpBizTypeEnum.PURCHASE_IN.getType(), 11L,
                ErpFinancePaymentAllocateStatusEnum.APPROVED.getStatus())).thenReturn(0L);
        when(erpFinancePrepaymentAllocateMapper.selectCountByBizTypeAndBizIdAndStatus(
                ErpBizTypeEnum.PURCHASE_IN.getType(), 11L,
                ErpFinancePrepaymentAllocateStatusEnum.APPROVED.getStatus())).thenReturn(1L);

        assertTrue(purchaseInQueryHelper.hasApprovedAllocate(11L));
    }

    @Test
    void updatePurchaseReturnStatus_shouldRejectUnapproveWhenPrepaymentAllocateExists() {
        when(erpPurchaseReturnMapper.selectById(21L)).thenReturn(new ErpPurchaseReturnDO()
                .setId(21L).setNo("PR-001").setStatus(ErpAuditStatus.APPROVE.getStatus())
                .setTotalPrice(new BigDecimal("88.00")));
        when(erpFinancePaymentAllocateMapper.selectCountByBizTypeAndBizIdAndStatus(
                ErpBizTypeEnum.PURCHASE_RETURN.getType(), 21L,
                ErpFinancePaymentAllocateStatusEnum.APPROVED.getStatus())).thenReturn(0L);
        when(erpFinancePrepaymentAllocateMapper.selectCountByBizTypeAndBizIdAndStatus(
                ErpBizTypeEnum.PURCHASE_RETURN.getType(), 21L,
                ErpFinancePrepaymentAllocateStatusEnum.APPROVED.getStatus())).thenReturn(1L);

        ServiceException ex = assertThrows(ServiceException.class, () ->
                purchaseReturnService.updatePurchaseReturnStatus(21L, ErpAuditStatus.PROCESS.getStatus()));

        assertEquals(PURCHASE_RETURN_PROCESS_FAIL_EXISTS_REFUND.getCode(), ex.getCode());
        // 守卫必须在状态 CAS 之前拦截，不允许先改状态再报错
        verify(erpPurchaseReturnMapper, never()).updateByIdAndStatus(any(), any(), any());
    }

}
