package cn.weitee.erp.module.erp.service.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityItemDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionOrderMapper;
import cn.weitee.erp.module.erp.enums.ErpPurchaseInQualityResultEnum;
import cn.weitee.erp.module.erp.enums.ErpPurchaseInQualityStatusEnum;
import cn.weitee.erp.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseInQualityService;
import cn.weitee.erp.module.erp.service.stock.ErpStockRecordService;
import cn.weitee.erp.module.erp.service.stock.ErpStockService;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockRecordCreateReqBO;
import cn.weitee.erp.module.erp.util.ErpTransactionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_STATUS_INVALID;

@Service
@Slf4j
public class ErpProductionIqcStockServiceImpl implements ErpProductionIqcStockService {

    @Resource
    private ErpProductionOrderMapper productionOrderMapper;
    @Resource
    private ErpStockService stockService;
    @Resource
    private ErpStockRecordService stockRecordService;
    @Resource
    private ErpPurchaseInQualityService purchaseInQualityService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductStockForProduction(Long productionOrderId, Long productId, BigDecimal qty) {
        if (productionOrderId == null || productId == null || qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(PRODUCTION_ORDER_STATUS_INVALID);
        }
        ErpProductionOrderDO order = productionOrderMapper.selectById(productionOrderId);
        if (order == null) {
            throw exception(PRODUCTION_ORDER_NOT_EXISTS);
        }
        if (order.getStatus() == null || order.getStatus() < 10) {
            throw exception(PRODUCTION_ORDER_STATUS_INVALID);
        }
        Long warehouseId = order.getWarehouseId();
        if (warehouseId == null) {
            log.error("[deductStockForProduction] 生产任务单未配置仓库，orderId={}", productionOrderId);
            throw exception(PRODUCTION_ORDER_STATUS_INVALID);
        }
        // 事务内仅校验，实际扣减在 afterCommit
        BigDecimal deductQty = qty.negate();
        String bizNo = order.getOrderNo() != null ? order.getOrderNo() : String.valueOf(productionOrderId);
        ErpTransactionUtils.afterCommit(() -> {
            try {
                stockService.updateStockCountIncrement(productId, warehouseId, deductQty);
                stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                        productId, warehouseId, deductQty,
                        ErpStockRecordBizTypeEnum.PRODUCTION_ISSUE.getType(),
                        productionOrderId, productId, bizNo,
                        null, null));
                log.info("[deductStockForProduction] 扣减库存成功，orderId={}, productId={}, warehouseId={}, qty={}",
                        productionOrderId, productId, warehouseId, qty);
            } catch (Exception e) {
                log.error("[deductStockForProduction] 扣减库存失败，orderId={}, productId={}, warehouseId={}, qty={}",
                        productionOrderId, productId, warehouseId, qty, e);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleIqcPassed(Long purchaseInId) {
        if (purchaseInId == null) {
            return;
        }
        ErpPurchaseInQualityDO quality = purchaseInQualityService.getPurchaseInQualityByPurchaseInId(purchaseInId);
        if (quality == null) {
            log.warn("[handleIqcPassed] 未找到质检单，purchaseInId={}", purchaseInId);
            return;
        }
        if (!ErpPurchaseInQualityStatusEnum.DONE.getStatus().equals(quality.getStatus())) {
            log.warn("[handleIqcPassed] 质检单未完成，qualityId={}, status={}", quality.getId(), quality.getStatus());
            return;
        }
        Integer result = quality.getResult();
        boolean passed = ErpPurchaseInQualityResultEnum.PASSED.getStatus().equals(result)
                || ErpPurchaseInQualityResultEnum.PARTIAL.getStatus().equals(result);
        if (!passed) {
            log.info("[handleIqcPassed] 质检未合格，不计入库存，qualityId={}, result={}", quality.getId(), result);
            return;
        }
        List<ErpPurchaseInQualityItemDO> items = purchaseInQualityService.getPurchaseInQualityItemListByQualityId(quality.getId());
        if (CollUtil.isEmpty(items)) {
            log.warn("[handleIqcPassed] 质检单无明细，qualityId={}", quality.getId());
            return;
        }
        String bizNo = quality.getPurchaseInNo() != null ? quality.getPurchaseInNo() : quality.getNo();
        if (bizNo == null) {
            bizNo = String.valueOf(purchaseInId);
        }
        String finalBizNo = bizNo;
        ErpTransactionUtils.afterCommit(() -> {
            for (ErpPurchaseInQualityItemDO item : items) {
                BigDecimal passCount = item.getQaPassCount();
                if (passCount == null || passCount.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                Long productId = item.getProductId();
                Long warehouseId = item.getWarehouseId();
                if (productId == null || warehouseId == null) {
                    log.warn("[handleIqcPassed] 明细缺产品或仓库，qualityItemId={}, productId={}, warehouseId={}",
                            item.getId(), productId, warehouseId);
                    continue;
                }
                try {
                    stockService.updateStockCountIncrement(productId, warehouseId, passCount);
                    stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                            productId, warehouseId, passCount,
                            ErpStockRecordBizTypeEnum.PURCHASE_IN.getType(),
                            purchaseInId, item.getPurchaseInItemId(), finalBizNo,
                            null, null));
                    log.info("[handleIqcPassed] IQC合格计入库存成功，purchaseInId={}, productId={}, warehouseId={}, passCount={}",
                            purchaseInId, productId, warehouseId, passCount);
                } catch (Exception e) {
                    log.error("[handleIqcPassed] 计入可用库存失败，purchaseInId={}, productId={}, warehouseId={}, passCount={}",
                            purchaseInId, productId, warehouseId, passCount, e);
                }
            }
        });
    }

}
