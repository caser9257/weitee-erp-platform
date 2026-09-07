package cn.weitee.erp.module.erp.service.mrp;

import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockTaskFailureLogDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionOrderMapper;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockMapper;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockTaskFailureLogMapper;
import cn.weitee.erp.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.weitee.erp.module.erp.event.ErpPurchaseInQualityFinishedEvent;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseInQualityService;
import cn.weitee.erp.module.erp.service.stock.ErpStockRecordService;
import cn.weitee.erp.module.erp.service.stock.ErpStockService;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockRecordCreateReqBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
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

    /**
     * 领料扣减可用库存。
     *
     * 调用契约：调用方（领料主流程）在本事务提交后的 afterCommit 阶段调用本方法，
     * 此时业务单据已提交，本方法内的任何失败都只能落失败记录待重试，不允许抛出——
     * 抛出既无法回滚已提交的领料单，还会被 afterCommit 调用链静默吞掉（历史缺陷）。
     * 因此前置校验必须前置到领料主事务内完成（见 ErpProductionIssueServiceImpl），
     * 这里保留校验仅作为其它调用路径的防御，且失败同样落失败记录。
     *
     * 仓库取自领料明细的发料仓（warehouseId 参数）：生产订单上的 warehouse_id 语义是
     * 完工入库仓（finish 流程才写入），在制品阶段的领料扣减不能使用（历史缺陷：语义错配）。
     */
    @Override
    public void deductStockForProduction(Long productionOrderId, Long productId, Long warehouseId, BigDecimal qty) {
        try {
            if (productionOrderId == null || productId == null || warehouseId == null
                    || qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
                throw exception(PRODUCTION_ORDER_STATUS_INVALID);
            }
            ErpProductionOrderDO order = productionOrderMapper.selectById(productionOrderId);
            if (order == null) {
                throw exception(PRODUCTION_ORDER_NOT_EXISTS);
            }
            if (order.getStatus() == null || order.getStatus() < 10) {
                throw exception(PRODUCTION_ORDER_STATUS_INVALID);
            }
            // 行锁新事务内 CAS 扣 available_count；失败抛出后统一落失败记录可重试。
            // 注意：不得在 afterCommit 阶段再注册嵌套 afterCommit——外层事务已提交，
            // 新注册的同步器不在已快照的回调列表里，永远不会执行（历史缺陷：扣减静默丢失）。
            newStockTxTemplate().executeWithoutResult(status -> deductCore(productId, warehouseId, qty));
            log.info("[deductStockForProduction] 可用库存扣减成功，orderId={}, productId={}, warehouseId={}, qty={}",
                    productionOrderId, productId, warehouseId, qty);
        } catch (Exception e) {
            log.error("[deductStockForProduction] 可用库存扣减失败，orderId={}, productId={}, warehouseId={}, qty={}",
                    productionOrderId, productId, warehouseId, qty, e);
            saveFailureLog(ErpStockTaskFailureLogDO.BIZ_TYPE_PRODUCTION_DEDUCT,
                    productionOrderId, null, productId, warehouseId, qty, e.getMessage());
        }
    }

    /**
     * 扣减核心：行锁下 CAS 扣 available_count。
     *
     * 分工约定：count 由发料主流程的 createStockRecord 流水机制维护，
     * 本方法只负责可用余额，避免双重计账；IQC 前置由"未过检不允许入库确认 +
     * 仅过检入库增加 available"等效保证。
     */
    private void deductCore(Long productId, Long warehouseId, BigDecimal qty) {
        ErpStockDO stock = stockMapper.selectByProductIdAndWarehouseIdForUpdate(productId, warehouseId);
        if (stock == null) {
            throw exception(STOCK_COUNT_NEGATIVE, "未知产品", "仓库" + warehouseId, BigDecimal.ZERO, qty);
        }
        int updated = stockMapper.updateAvailableCountIncrement(stock.getId(), qty.negate(), false);
        if (updated == 0) {
            throw exception(STOCK_COUNT_NEGATIVE, productId.toString(), "仓库" + warehouseId,
                    stock.getAvailableCount(), qty);
        }
    }

    // ========== IQC 合格移可用 ==========

    /**
     * IQC 质检完成回调（单写者约定下的无库存动作）。
     *
     * 可用库存的唯一记账人是采购入库确认（ErpPurchaseInStockExecuteHelper#increaseAvailableCount，
     * "过检即可用"，按实入量同步事务入账，CAS 失败整体回滚）。本方法不再写库存——
     * 历史版本在此按 qaPassCount 再加一次 available，与确认入库形成双写，
     * 谁先谁后产生两种不同账目（缺陷：同一笔入库 available 双计）。
     * 保留方法与事件接线用于审计与未来扩展；retryStockTaskFailure 对历史
     * IQC_MOVE 失败记录的重试仍走 moveAvailableCore，保持兼容。
     */
    @Override
    public void handleIqcPassed(Long purchaseInId) {
        log.info("[handleIqcPassed] IQC 质检完成，可用库存由确认入库统一入账，无需处理，purchaseInId={}", purchaseInId);
    }

    /**
     * 消费质检完结事件：事务提交后执行 IQC 合格移可用。
     *
     * <p>接线点为质检单 DONE 终态写入源头（finishQualityOrder 发布事件），
     * 本监听只做转发，DONE/PASSED/PARTIAL 校验与幂等均由 {@link #handleIqcPassed} 及
     * moveAvailableCore 的流水锚点保证。
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onPurchaseInQualityFinished(ErpPurchaseInQualityFinishedEvent event) {
        handleIqcPassed(event.getPurchaseInId());
    }

    /**
     * 移可用核心：行锁下按最新暂存数切分——
     * 可用恒加全额；暂存足额部分从 quality_hold 扣除且总量不变；不足部分作为新货计入总量。
     */
    private void moveAvailableCore(Long purchaseInId, Long itemId, String bizNo,
                                   Long productId, Long warehouseId, BigDecimal passCount) {
        // 幂等锚点：同一入库明细已存在 PURCHASE_IN 流水说明移可用已完成，直接跳过，
        // 防御事件重复投递、手工重试与正常流转撞车导致的重复加库存
        if (stockRecordService.hasStockRecord(ErpStockRecordBizTypeEnum.PURCHASE_IN.getType(), purchaseInId, itemId)) {
            log.info("[moveAvailableCore] 库存流水已存在，跳过重复移可用，purchaseInId={}, itemId={}", purchaseInId, itemId);
            return;
        }
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
                newStockTxTemplate().executeWithoutResult(status -> deductCore(failureLog.getProductId(),
                        failureLog.getWarehouseId(), failureLog.getQty()));
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
