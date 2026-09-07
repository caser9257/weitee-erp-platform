package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePaymentAllocateDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePrepaymentAllocateDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpApStatementItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpApStatementMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceExpenseMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePaymentAllocateMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePrepaymentAllocateMapper;
import cn.weitee.erp.module.erp.enums.ErpApStatementStatusEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseInService;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseReturnService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.AP_STATEMENT_HAS_APPROVED_ALLOCATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * P2 预备：closeStatementByBiz 机制级守卫测试。
 * 台账存在已生效核销（付款或预付款）时禁止关闭，防止出现"台账已关闭 + 核销事实仍生效"的半条 AP 事实。
 */
@ExtendWith(MockitoExtension.class)
class ErpApStatementCloseGuardTest {

    @InjectMocks
    private ErpApStatementServiceImpl service;

    @Mock
    private ErpApStatementMapper erpApStatementMapper;
    @Mock
    private ErpApStatementItemMapper erpApStatementItemMapper;
    @Mock
    private ErpFinanceExpenseMapper erpFinanceExpenseMapper;
    @Mock
    private ErpFinancePaymentAllocateMapper erpFinancePaymentAllocateMapper;
    @Mock
    private ErpFinancePrepaymentAllocateMapper erpFinancePrepaymentAllocateMapper;
    @Mock
    private ErpPurchaseInService purchaseInService;
    @Mock
    private ErpPurchaseReturnService purchaseReturnService;
    @Mock
    private ErpApEstimateService apEstimateService;

    private ErpApStatementDO openStatement() {
        return new ErpApStatementDO().setId(1L).setStatementNo("AP-PI-1")
                .setBizType(ErpBizTypeEnum.PURCHASE_IN.getType()).setBizId(11L).setBizNo("PI-001")
                .setAmount(new BigDecimal("100.00")).setPaidAmount(BigDecimal.ZERO)
                .setRemainAmount(new BigDecimal("100.00"))
                .setStatus(ErpApStatementStatusEnum.UNPAID.getStatus());
    }

    @Test
    void closeStatementByBiz_shouldThrowWhenApprovedPaymentAllocateExists() {
        when(erpApStatementMapper.selectByBizTypeAndBizId(ErpBizTypeEnum.PURCHASE_IN.getType(), 11L))
                .thenReturn(openStatement());
        when(erpFinancePaymentAllocateMapper.selectApprovedListByStatementIds(anyCollection()))
                .thenReturn(List.of(new ErpFinancePaymentAllocateDO().setId(901L).setApStatementId(1L)));

        ServiceException ex = assertThrows(ServiceException.class, () ->
                service.closeStatementByBiz(ErpBizTypeEnum.PURCHASE_IN.getType(), 11L, "反审核"));

        assertEquals(AP_STATEMENT_HAS_APPROVED_ALLOCATE.getCode(), ex.getCode());
        verify(erpApStatementMapper, never()).updateById(any(ErpApStatementDO.class));
    }

    @Test
    void closeStatementByBiz_shouldThrowWhenApprovedPrepaymentAllocateExists() {
        when(erpApStatementMapper.selectByBizTypeAndBizId(ErpBizTypeEnum.PURCHASE_IN.getType(), 11L))
                .thenReturn(openStatement());
        when(erpFinancePaymentAllocateMapper.selectApprovedListByStatementIds(anyCollection()))
                .thenReturn(List.of());
        when(erpFinancePrepaymentAllocateMapper.selectApprovedListByStatementIds(anyCollection()))
                .thenReturn(List.of(new ErpFinancePrepaymentAllocateDO().setId(902L).setApStatementId(1L)));

        ServiceException ex = assertThrows(ServiceException.class, () ->
                service.closeStatementByBiz(ErpBizTypeEnum.PURCHASE_IN.getType(), 11L, "反审核"));

        assertEquals(AP_STATEMENT_HAS_APPROVED_ALLOCATE.getCode(), ex.getCode());
        verify(erpApStatementMapper, never()).updateById(any(ErpApStatementDO.class));
    }

    @Test
    void closeStatementByBiz_shouldCloseWhenNoApprovedAllocate() {
        when(erpApStatementMapper.selectByBizTypeAndBizId(ErpBizTypeEnum.PURCHASE_IN.getType(), 11L))
                .thenReturn(openStatement());
        when(erpFinancePaymentAllocateMapper.selectApprovedListByStatementIds(anyCollection()))
                .thenReturn(List.of());
        when(erpFinancePrepaymentAllocateMapper.selectApprovedListByStatementIds(anyCollection()))
                .thenReturn(List.of());

        service.closeStatementByBiz(ErpBizTypeEnum.PURCHASE_IN.getType(), 11L, "反审核");

        verify(erpApStatementMapper).updateById(any(ErpApStatementDO.class));
    }

    @Test
    void closeStatementByBiz_shouldSkipWhenAlreadyClosed() {
        ErpApStatementDO closed = openStatement().setStatus(ErpApStatementStatusEnum.CLOSED.getStatus());
        when(erpApStatementMapper.selectByBizTypeAndBizId(ErpBizTypeEnum.PURCHASE_IN.getType(), 11L))
                .thenReturn(closed);

        service.closeStatementByBiz(ErpBizTypeEnum.PURCHASE_IN.getType(), 11L, "重复关闭");

        verify(erpApStatementMapper, never()).updateById(any(ErpApStatementDO.class));
    }

}
