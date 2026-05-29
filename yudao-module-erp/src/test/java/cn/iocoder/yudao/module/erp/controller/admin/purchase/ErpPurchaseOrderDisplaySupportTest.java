package cn.iocoder.yudao.module.erp.controller.admin.purchase;

import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.ErpPurchaseInStockInStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpQaStatusEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErpPurchaseOrderDisplaySupportTest {

    @Test
    void hasPendingPurchaseIn_shouldReturnTrueWhenDraftOrProcessingExists() {
        List<ErpPurchaseInDO> purchaseIns = List.of(
                new ErpPurchaseInDO().setStatus(ErpAuditStatus.PROCESS.getStatus()),
                new ErpPurchaseInDO().setStatus(ErpAuditStatus.REJECT.getStatus()));

        assertTrue(ErpPurchaseOrderDisplaySupport.hasPendingPurchaseIn(purchaseIns));
    }

    @Test
    void hasPendingPurchaseIn_shouldReturnTrueWhenApprovedButStillWaitingForQualityCheck() {
        List<ErpPurchaseInDO> purchaseIns = List.of(
                new ErpPurchaseInDO().setId(12L)
                        .setStatus(ErpAuditStatus.APPROVE.getStatus())
                        .setQaStatus(ErpQaStatusEnum.TO_INSPECT.getStatus()));

        assertTrue(ErpPurchaseOrderDisplaySupport.hasPendingPurchaseIn(purchaseIns));
        assertEquals(12L, ErpPurchaseOrderDisplaySupport.resolvePendingPurchaseInId(purchaseIns));
    }

    @Test
    void hasPendingPurchaseIn_shouldReturnTrueWhenApprovedButStillWaitingForStockIn() {
        List<ErpPurchaseInDO> purchaseIns = List.of(
                new ErpPurchaseInDO().setId(31L)
                        .setStatus(ErpAuditStatus.APPROVE.getStatus())
                        .setQaStatus(ErpQaStatusEnum.PASSED.getStatus())
                        .setStockInStatus(ErpPurchaseInStockInStatusEnum.TO_STOCK_IN.getStatus()),
                new ErpPurchaseInDO().setId(32L)
                        .setStatus(ErpAuditStatus.APPROVE.getStatus())
                        .setQaStatus(ErpQaStatusEnum.PARTIAL.getStatus())
                        .setStockInStatus(ErpPurchaseInStockInStatusEnum.PARTIAL_STOCKED_IN.getStatus()));

        assertTrue(ErpPurchaseOrderDisplaySupport.hasPendingPurchaseIn(purchaseIns));
        assertEquals(31L, ErpPurchaseOrderDisplaySupport.resolvePendingPurchaseInId(purchaseIns));
    }

    @Test
    void hasPendingPurchaseIn_shouldReturnFalseWhenAllPurchaseInsFinishedQualityCheck() {
        List<ErpPurchaseInDO> purchaseIns = List.of(
                new ErpPurchaseInDO().setStatus(ErpAuditStatus.APPROVE.getStatus())
                        .setQaStatus(ErpQaStatusEnum.PASSED.getStatus())
                        .setStockInStatus(ErpPurchaseInStockInStatusEnum.STOCKED_IN.getStatus()),
                new ErpPurchaseInDO().setStatus(ErpAuditStatus.APPROVE.getStatus())
                        .setQaStatus(ErpQaStatusEnum.PARTIAL.getStatus())
                        .setStockInStatus(ErpPurchaseInStockInStatusEnum.STOCKED_IN.getStatus()),
                new ErpPurchaseInDO().setStatus(ErpAuditStatus.APPROVE.getStatus())
                        .setQaStatus(ErpQaStatusEnum.REJECTED.getStatus())
                        .setStockInStatus(ErpPurchaseInStockInStatusEnum.NO_NEED_STOCK_IN.getStatus()));

        assertFalse(ErpPurchaseOrderDisplaySupport.hasPendingPurchaseIn(purchaseIns));
        assertNull(ErpPurchaseOrderDisplaySupport.resolvePendingPurchaseInId(purchaseIns));
    }

    @Test
    void resolvePendingPurchaseInId_shouldPreferFirstPendingPurchaseIn() {
        List<ErpPurchaseInDO> purchaseIns = List.of(
                new ErpPurchaseInDO().setId(21L)
                        .setStatus(ErpAuditStatus.APPROVE.getStatus())
                        .setQaStatus(ErpQaStatusEnum.PASSED.getStatus()),
                new ErpPurchaseInDO().setId(22L)
                        .setStatus(ErpAuditStatus.PROCESS.getStatus()),
                new ErpPurchaseInDO().setId(23L)
                        .setStatus(ErpAuditStatus.REJECT.getStatus()));

        assertEquals(22L, ErpPurchaseOrderDisplaySupport.resolvePendingPurchaseInId(purchaseIns));
    }

    @Test
    void shouldDisplayPendingPurchaseIn_shouldReturnFalseWhenInboundAlreadyCompleted() {
        List<ErpPurchaseInDO> purchaseIns = List.of(
                new ErpPurchaseInDO().setId(31L)
                        .setStatus(ErpAuditStatus.APPROVE.getStatus())
                        .setQaStatus(ErpQaStatusEnum.PARTIAL.getStatus())
                        .setStockInStatus(ErpPurchaseInStockInStatusEnum.PARTIAL_STOCKED_IN.getStatus()));

        assertFalse(ErpPurchaseOrderDisplaySupport.shouldDisplayPendingPurchaseIn(
                BigDecimal.TEN, BigDecimal.TEN, purchaseIns));
        assertNull(ErpPurchaseOrderDisplaySupport.resolveDisplayPendingPurchaseInId(
                BigDecimal.TEN, BigDecimal.TEN, purchaseIns));
    }
}
