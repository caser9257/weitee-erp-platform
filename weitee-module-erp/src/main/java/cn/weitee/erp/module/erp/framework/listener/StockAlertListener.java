package cn.weitee.erp.module.erp.framework.listener;

import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpPurchaseSuggestDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpPurchaseSuggestMapper;
import cn.weitee.erp.module.erp.enums.mrp.ErpMrpSuggestStatusEnum;
import cn.weitee.erp.module.erp.framework.event.StockBelowSafetyEvent;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 库存预警监听器
 *
 * 当库存低于安全库存时，自动创建采购建议。
 * 使用 @TransactionalEventListener 确保主事务提交后才执行，避免事务回滚导致数据不一致。
 */
@Component
@Slf4j
public class StockAlertListener {

    /**
     * 库存预警自动生成的备注标识，用于防重检查
     * TODO: 后续在 erp_purchase_suggest 增加 source_type 字段后改为精确条件查询
     */
    private static final String STOCK_ALERT_REMARK_PREFIX = "[库存预警:仓库";

    private String buildStockAlertRemark(Long warehouseId) {
        return STOCK_ALERT_REMARK_PREFIX + warehouseId + "]";
    }

    @Resource
    private ErpPurchaseSuggestMapper erpPurchaseSuggestMapper;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private PlatformTransactionManager transactionManager;

    /**
     * 处理库存低于安全库存事件
     * <p>
     * - @TransactionalEventListener(AFTER_COMMIT)：仅在事务提交后触发，防止主事务回滚后脏数据
     * - 注意：不使用 @Async，因为 @Async 会在新线程执行，导致 AFTER_COMMIT 语义不可靠
     *   （原始事务上下文在新线程中不存在，事件可能在事务未提交时就开始处理）
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStockBelowSafety(StockBelowSafetyEvent event) {
        handleStockBelowSafety(event);
    }

    private void handleStockBelowSafety(StockBelowSafetyEvent event) {
        try {
            log.info("[onStockBelowSafety] 库存预警：productId={}, warehouseId={}, current={}, safety={}, shortage={}",
                    event.getProductId(), event.getWarehouseId(), event.getCurrentCount(), event.getSafetyStock(), event.getShortage());

            RLock lock = redissonClient.getLock("erp:stock-alert:create-purchase-suggest:"
                    + event.getProductId() + ":" + event.getWarehouseId());
            if (!lock.tryLock()) {
                log.info("[onStockBelowSafety] 库存预警建议正在创建中，跳过，productId={}, warehouseId={}",
                        event.getProductId(), event.getWarehouseId());
                return;
            }
            try {
                executeInRequiresNewTransaction(() -> createPurchaseSuggestIfAbsent(event));
            } finally {
                unlockIfHeld(lock);
            }

        } catch (Exception e) {
            // TODO: 后续增加失败重试机制（定时扫描/告警通知），避免采购建议在异常时被永久丢失
            log.error("[onStockBelowSafety] 处理库存预警事件失败，需人工介入或定时重试，productId={}, warehouseId={}",
                    event.getProductId(), event.getWarehouseId(), e);
        }
    }

    private void executeInRequiresNewTransaction(Runnable runnable) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.executeWithoutResult(status -> runnable.run());
    }

    private void createPurchaseSuggestIfAbsent(StockBelowSafetyEvent event) {

            // 1. 检查是否已有待处理的库存预警建议（防重复，按 productId + warehouseId）
            String stockAlertRemark = buildStockAlertRemark(event.getWarehouseId());
            if (existsPendingStockAlertSuggest(event.getProductId(), stockAlertRemark)) {
                log.info("[onStockBelowSafety] 已有待处理的库存预警建议，跳过，productId={}, warehouseId={}",
                        event.getProductId(), event.getWarehouseId());
                return;
            }

            // 2. 计算建议采购数量（缺口 + 20% 缓冲，向上取整）
            // TODO: 缓冲比例和默认到货天数应从 MRP 规则或系统参数读取
            BigDecimal suggestQty = event.getShortage()
                    .multiply(new BigDecimal("1.2"))
                    .setScale(0, java.math.RoundingMode.CEILING);

            // 3. 创建采购建议
            ErpPurchaseSuggestDO suggest = ErpPurchaseSuggestDO.builder()
                    .materialId(event.getProductId())
                    .warehouseId(event.getWarehouseId())
                    .suggestQty(suggestQty)
                    .suggestArrivalDate(LocalDate.now().plusDays(7)) // TODO: 从规则读取默认到货天数
                    .availableStockQty(event.getCurrentCount())
                    .incomingQty(BigDecimal.ZERO)
                    .wipQty(BigDecimal.ZERO)
                    .reservedStockQty(BigDecimal.ZERO)
                    .safetyStockQty(event.getSafetyStock())
                    .netDemandQty(event.getShortage())
                    .status(ErpMrpSuggestStatusEnum.TO_CONFIRM.getStatus())
                    .remark(stockAlertRemark)
                    .build();
            erpPurchaseSuggestMapper.insert(suggest);

            log.info("[onStockBelowSafety] 采购建议创建成功，id={}, productId={}, warehouseId={}, qty={}",
                    suggest.getId(), event.getProductId(), event.getWarehouseId(), suggestQty);
    }

    private void unlockIfHeld(RLock lock) {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    /**
     * 检查是否已存在该产品的待处理库存预警建议
     * 使用 like 前缀匹配，比精确匹配更健壮（即使备注被追加其他内容也能识别）
     */
    private boolean existsPendingStockAlertSuggest(Long productId, String stockAlertRemark) {
        Long count = erpPurchaseSuggestMapper.selectCount(
                new LambdaQueryWrapper<ErpPurchaseSuggestDO>()
                        .eq(ErpPurchaseSuggestDO::getMaterialId, productId)
                        .in(ErpPurchaseSuggestDO::getStatus,
                                ErpMrpSuggestStatusEnum.TO_CONFIRM.getStatus(),
                                ErpMrpSuggestStatusEnum.CONFIRMED.getStatus())
                        .likeRight(ErpPurchaseSuggestDO::getRemark, STOCK_ALERT_REMARK_PREFIX));
        return count > 0;
    }

}
