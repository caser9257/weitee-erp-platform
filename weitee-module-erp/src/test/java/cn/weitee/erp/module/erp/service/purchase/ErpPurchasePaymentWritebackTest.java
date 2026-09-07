package cn.weitee.erp.module.erp.service.purchase;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseReturnMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_FAIL_PAYMENT_PRICE_EXCEED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_RETURN_FAIL_REFUND_PRICE_EXCEED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_RETURN_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * P2 预备：AP 台账回写来源单据方法的健壮性测试。
 * 台账 bizId 悬挂（来源单不存在）时必须快速失败为明确业务错误（事务整体回滚），禁止 NPE；
 * 金额比较必须 null 安全且不受 BigDecimal scale 影响。
 */
@ExtendWith(MockitoExtension.class)
class ErpPurchasePaymentWritebackTest {

    @InjectMocks
    private ErpPurchaseInServiceImpl purchaseInService;
    @InjectMocks
    private ErpPurchaseReturnServiceImpl purchaseReturnService;

    @Mock
    private ErpPurchaseInMapper erpPurchaseInMapper;
    @Mock
    private ErpPurchaseReturnMapper erpPurchaseReturnMapper;

    @Test
    void updatePurchaseInPaymentPrice_shouldFailFastWhenPurchaseInMissing() {
        when(erpPurchaseInMapper.selectById(99L)).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class, () ->
                purchaseInService.updatePurchaseInPaymentPrice(99L, new BigDecimal("10.00")));

        assertEquals(PURCHASE_IN_NOT_EXISTS.getCode(), ex.getCode());
        verify(erpPurchaseInMapper, never()).updateById(any(ErpPurchaseInDO.class));
    }

    @Test
    void updatePurchaseInPaymentPrice_shouldTreatNullCurrentAsZeroAndSkipNoop() {
        when(erpPurchaseInMapper.selectById(1L)).thenReturn(new ErpPurchaseInDO()
                .setId(1L).setTotalPrice(new BigDecimal("100.00")).setPaymentPrice(null));

        purchaseInService.updatePurchaseInPaymentPrice(1L, new BigDecimal("0.00"));

        verify(erpPurchaseInMapper, never()).updateById(any(ErpPurchaseInDO.class));
    }

    @Test
    void updatePurchaseInPaymentPrice_shouldUpdateAndRejectExceed() {
        when(erpPurchaseInMapper.selectById(2L)).thenReturn(new ErpPurchaseInDO()
                .setId(2L).setTotalPrice(new BigDecimal("100.00")).setPaymentPrice(new BigDecimal("0.00")));

        purchaseInService.updatePurchaseInPaymentPrice(2L, new BigDecimal("40.00"));
        ArgumentCaptor<ErpPurchaseInDO> captor = ArgumentCaptor.forClass(ErpPurchaseInDO.class);
        verify(erpPurchaseInMapper).updateById(captor.capture());
        assertEquals(new BigDecimal("40.00"), captor.getValue().getPaymentPrice());

        ServiceException ex = assertThrows(ServiceException.class, () ->
                purchaseInService.updatePurchaseInPaymentPrice(2L, new BigDecimal("120.00")));
        assertEquals(PURCHASE_IN_FAIL_PAYMENT_PRICE_EXCEED.getCode(), ex.getCode());
    }

    @Test
    void updatePurchaseReturnRefundPrice_shouldFailFastWhenReturnMissing() {
        when(erpPurchaseReturnMapper.selectById(98L)).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class, () ->
                purchaseReturnService.updatePurchaseReturnRefundPrice(98L, new BigDecimal("10.00")));

        assertEquals(PURCHASE_RETURN_NOT_EXISTS.getCode(), ex.getCode());
        verify(erpPurchaseReturnMapper, never()).updateById(any(ErpPurchaseReturnDO.class));
    }

    @Test
    void updatePurchaseReturnRefundPrice_shouldCompareScaleInsensitivelyAndRejectExceed() {
        when(erpPurchaseReturnMapper.selectById(3L)).thenReturn(new ErpPurchaseReturnDO()
                .setId(3L).setTotalPrice(new BigDecimal("88.00")).setRefundPrice(new BigDecimal("0")));

        // 0 vs 0.00：scale 不同但数值相同，必须视为无变化（避免无意义更新）
        purchaseReturnService.updatePurchaseReturnRefundPrice(3L, new BigDecimal("0.00"));
        verify(erpPurchaseReturnMapper, never()).updateById(any(ErpPurchaseReturnDO.class));

        purchaseReturnService.updatePurchaseReturnRefundPrice(3L, new BigDecimal("30.00"));
        verify(erpPurchaseReturnMapper).updateById(any(ErpPurchaseReturnDO.class));

        ServiceException ex = assertThrows(ServiceException.class, () ->
                purchaseReturnService.updatePurchaseReturnRefundPrice(3L, new BigDecimal("90.00")));
        assertEquals(PURCHASE_RETURN_FAIL_REFUND_PRICE_EXCEED.getCode(), ex.getCode());
    }
}
