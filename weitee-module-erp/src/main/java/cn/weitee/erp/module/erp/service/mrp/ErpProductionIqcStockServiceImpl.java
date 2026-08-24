package cn.weitee.erp.module.erp.service.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockTaskFailureLogDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionOrderMapper;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockMapper;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockTaskFailureLogMapper;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_STATUS_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.STOCK_COUNT_NEGATIVE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.STOCK_TASK_FAILURE_ALREADY_RESOLVED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.STOCK_TASK_FAILURE_NOT_EXISTS;

@Service
@Slf4j
public class ErpProductionIqcStockServiceImpl implements ErpProductionIqcStockService {

    @Resource
    private ErpProductionOrderMapper productionOrderMapper;
    @Resource
    private ErpStockService stockService;
    @Resource
    private ErpStockMapper stockMapper;
    @Resource
    private ErpStockRecordService stockRecordService;
    @Resource
    private ErpPurchaseInQualityService purchaseInQualityService;
    @Resource
    private ErpStockTaskFailureLogMapper stockTaskFailureLogMapper;
    @Resource
    private PlatformTransactionManager transactionManager;

    /**
     * 库存行锁事务模板：afterCommit 中已无事务，库存变更必须在新事务内配合 FOR UPDATE 行锁执行，
     * 消除"读-判-改"竞态（TOCTOU），并保证多字段更新与库存流水同事务原子提交。
     */
    private TransactionTemplate newStockTxTemplate() {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return template;
    }

    // ========== 生产领料扣减 ==========

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
        // 事务内仅校验，实际扣减在 afterCommit 的行锁事务中执行；失败落库可重试
        ErpTransactionUtils.afterCommit(() -> {
            try {
                newStockTxTemplate().executeWithoutResult(
                        status -> deductCore(productionOrderId, productId, warehouseId, qty));
                log.info("[deductStockForProduction] 扣减库存成功，orderId={}, productId={}, warehouseId={}, qty={}",
                        productionOrderId, productId, warehouseId, qty);
            } catch (Exception e) {
                log.error("[deductStockForProduction] 扣减库存失败，orderId={}, productId={}, warehouseId={}, qty={}",
                        productionOrderId, productId, warehouseId, qty, e);
                saveFailureLog(ErpStockTaskFailureLogDO.BIZ_TYPE_PRODUCTION_DEDUCT,
                        productionOrderId, null, productId, warehouseId, qty, e.getMessage());
            }
        });
    }

    /**
     * 扣减核心：行锁下 CAS 扣可用与总量，并同事务写库存流水。
     */
    private void deductCore(Long productionOrderId, Long productId, Long warehouseId, BigDecimal qty) {
        ErpProductionOrderDO order = productionOrderMapper.selectById(productionOrderId);
        String bizNo = order != null && order.getOrderNo() != null ? order.getOrderNo() : String.valueOf(productionOrderId);
        ErpStockDO stock = stockMapper.selectByProductIdAndWarehouseIdForUpdate(productId, warehouseId);
        if (stock == null) {
            throw exception(STOCK_COUNT_NEGATIVE, "未知产品", "仓库" + warehouseId, BigDecimal.ZERO, qty);
        }
        int c1 = stockMapper.updateAvailableCountIncrement(stock.getId(), qty.negate(), false);
        int c2 = stockMapper.updateCountIncrement(stock.getId(), qty.negate(), false);
        if (c1 == 0 || c2 == 0) {
            throw exception(STOCK_COUNT_NEGATIVE, productId.toString(), "仓库" + warehouseId,
                    stock.getAvailableCount(), qty);
        }
        stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                productId, warehouseId, qty.negate(),
                ErpStockRecordBizTypeEnum.PRODUCTION_ISSUE.getType(),
                productionOrderId, productId, bizNo,
                null, null));
    }

    // ========== IQC 合格移可用 ==========

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
                    newStockTxTemplate().executeWithoutResult(
                            status -> moveAvailableCore(purchaseInId, item.getId(), finalBizNo,
                                    productId, warehouseId, passCount));
                    log.info("[handleIqcPassed] IQC合格计入库存成功，purchaseInId={}, productId={}, warehouseId={}, passCount={}",
                            purchaseInId, productId, warehouseId, passCount);
                } catch (Exception e) {
                    log.error("[handleIqcPassed] 计入可用库存失败，purchaseInId={}, productId={}, warehouseId={}, passCount={}",
                            purchaseInId, productId, warehouseId, passCount, e);
                    saveFailureLog(ErpStockTaskFailureLogDO.BIZ_TYPE_IQC_MOVE_AVAILABLE,
                            purchaseInId, item.getId(), productId, warehouseId, passCount, e.getMessage());
                }
            }
        });
    }

    /**
     * 移可用核心：行锁下按最新暂存数切分——
     * 可用恒加全额；暂存足额部分从 quality_hold 扣除且总量不变；不足部分作为新货计入总量。
     */
    private void moveAvailableCore(Long purchaseInId, Long itemId, String bizNo,
                                   Long productId, Long warehouseId, BigDecimal passCount) {
        ErpStockDO stock = stockMapper.selectByProductIdAndWarehouseIdForUpdate(productId, warehouseId);
        if (stock == null) {
            // 无库存行：先建行累加总量，再补可用
            stockService.updateStockCountIncrement(productId, warehouseId, passCount);
            ErpStockDO fresh = stockMapper.selectByProductIdAndWarehouseIdForUpdate(productId, warehouseId);
            if (fresh != null) {
                stockMapper.updateAvailableCountIncrement(fresh.getId(), passCount, false);
            }
        } else {
            BigDecimal hold = stock.getQualityHoldCount() == null ? BigDecimal.ZERO : stock.getQualityHoldCount();
            BigDecimal take = passCount.min(hold.max(BigDecimal.ZERO));
            if (take.compareTo(BigDecimal.ZERO) > 0) {
                stockMapper.updateQualityHoldCountIncrement(stock.getId(), take.negate());
            }
            stockMapper.updateAvailableCountIncrement(stock.getId(), passCount, false);
            BigDecimal rest = passCount.subtract(take);
            if (rest.compareTo(BigDecimal.ZERO) > 0) {
                stockMapper.updateCountIncrement(stock.getId(), rest, false);
            }
        }
        stockRecordService.createStockRecord(new ErpStockRecordCreateReqBO(
                productId, warehouseId, passCount,
                ErpStockRecordBizTypeEnum.PURCHASE_IN.getType(),
                purchaseInId, itemId, bizNo,
                null, null));
    }

    // ========== 失败记录与重试 ==========

    private void saveFailureLog(String bizType, Long bizId, Long itemId,
                                Long productId, Long warehouseId, BigDecimal qty, String reason) {
        try {
            newStockTxTemplate().executeWithoutResult(status -> stockTaskFailureLogMapper.insert(
                    ErpStockTaskFailureLogDO.builder()
                            .bizType(bizType).bizId(bizId).itemId(itemId)
                            .productId(productId).warehouseId(warehouseId)
                            .qty(qty)
                            .reason(StrUtil.maxLength(reason, 500))
                            .status(ErpStockTaskFailureLogDO.STATUS_PENDING_RETRY)
                            .retryCount(0)
                            .build()));
            log.warn("[saveFailureLog] 已登记失败任务待重试，bizType={}, bizId={}, productId={}",
                    bizType, bizId, productId);
        } catch (Exception ex) {
            log.error("[saveFailureLog] 登记失败任务也失败，需人工介入，bizType={}, bizId={}, productId={}",
                    bizType, bizId, productId, ex);
        }
    }

    @Override
    public List<ErpStockTaskFailureLogDO> getStockTaskFailureLogList(Integer status) {
        return stockTaskFailureLogMapper.selectListByStatus(status);
    }

    @Override
    public void retryStockTaskFailure(Long id) {
        ErpStockTaskFailureLogDO failureLog = stockTaskFailureLogMapper.selectById(id);
        if (failureLog == null) {
            throw exception(STOCK_TASK_FAILURE_NOT_EXISTS);
        }
        if (ErpStockTaskFailureLogDO.STATUS_RESOLVED.equals(failureLog.getStatus())) {
            throw exception(STOCK_TASK_FAILURE_ALREADY_RESOLVED);
        }
        try {
            if (ErpStockTaskFailureLogDO.BIZ_TYPE_PRODUCTION_DEDUCT.equals(failureLog.getBizType())) {
                newStockTxTemplate().executeWithoutResult(status -> deductCore(failureLog.getBizId(),
                        failureLog.getProductId(), failureLog.getWarehouseId(), failureLog.getQty()));
            } else {
                String bizNo = resolveIqcBizNo(failureLog.getBizId());
                newStockTxTemplate().executeWithoutResult(st -> moveAvailableCore(failureLog.getBizId(),
                        failureLog.getItemId(), bizNo,
                        failureLog.getProductId(), failureLog.getWarehouseId(), failureLog.getQty()));
            }
        } catch (Exception e) {
            markRetryFailed(failureLog, e.getMessage());
            throw e;
        }
        markResolved(failureLog);
    }

    private String resolveIqcBizNo(Long purchaseInId) {
        ErpPurchaseInQualityDO quality = purchaseInQualityService.getPurchaseInQualityByPurchaseInId(purchaseInId);
        if (quality != null && quality.getPurchaseInNo() != null) {
            return quality.getPurchaseInNo();
        }
        if (quality != null && quality.getNo() != null) {
            return quality.getNo();
        }
        return String.valueOf(purchaseInId);
    }

    private void markRetryFailed(ErpStockTaskFailureLogDO failureLog, String reason) {
        try {
            newStockTxTemplate().executeWithoutResult(status -> stockTaskFailureLogMapper.updateById(
                    new ErpStockTaskFailureLogDO()
                            .setId(failureLog.getId())
                            .setRetryCount((failureLog.getRetryCount() == null ? 0 : failureLog.getRetryCount()) + 1)
                            .setReason(StrUtil.maxLength(reason, 500))));
        } catch (Exception ex) {
            log.error("[markRetryFailed] 更新重试次数失败，id={}", failureLog.getId(), ex);
        }
    }

    private void markResolved(ErpStockTaskFailureLogDO failureLog) {
        newStockTxTemplate().executeWithoutResult(status -> stockTaskFailureLogMapper.updateById(
                new ErpStockTaskFailureLogDO()
                        .setId(failureLog.getId())
                        .setStatus(ErpStockTaskFailureLogDO.STATUS_RESOLVED)));
    }

}
