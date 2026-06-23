package cn.iocoder.yudao.module.erp.framework.listener;

import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpPurchaseSuggestDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpPurchaseSuggestMapper;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpMrpSuggestStatusEnum;
import cn.iocoder.yudao.module.erp.framework.event.StockBelowSafetyEvent;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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
    private static final String STOCK_ALERT_REMARK = "[库存预警]";

    @Resource
    private ErpPurchaseSuggestMapper erpPurchaseSuggestMapper;

    /**
     * 处理库存低于安全库存事件
     * <p>
     * - @Async：异步执行，不阻塞主流程
     * - @TransactionalEventListener(AFTER_COMMIT)：仅在事务提交后触发，防止主事务回滚后脏数据
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStockBelowSafety(StockBelowSafetyEvent event) {
        try {
            log.info("[onStockBelowSafety] 库存预警：productId={}, current=, safety={}, shortage={}",
                    event.getProductId(), event.getCurrentCount(), event.getSafetyStock(), event.getShortage());

            // 1. 检查是否已有待处理的库存预警建议（防重复）
            if (existsPendingStockAlertSuggest(event.getProductId())) {
                log.info("[onStockBelowSafety] 已有待处理的库存预警建议，跳过，productId={}", event.getProductId());
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
                    .suggestQty(suggestQty)
                    .suggestArrivalDate(LocalDate.now().plusDays(7)) // TODO: 从规则读取默认到货天数
                    .availableStockQty(event.getCurrentCount())
                    .incomingQty(BigDecimal.ZERO)
                    .wipQty(BigDecimal.ZERO)
                    .reservedStockQty(BigDecimal.ZERO)
                    .safetyStockQty(event.getSafetyStock())
                    .netDemandQty(event.getShortage())
                    .status(ErpMrpSuggestStatusEnum.TO_CONFIRM.getStatus())
                    .remark(STOCK_ALERT_REMARK)
                    .build();
            erpPurchaseSuggestMapper.insert(suggest);

            log.info("[onStockBelowSafety] 采购建议创建成功，id={}, productId={}, qty={}",
                    suggest.getId(), event.getProductId(), suggestQty);

        } catch (Exception e) {
            log.error("[onStockBelowSafety] 处理库存预警事件失败，productId={}", event.getProductId(), e);
        }
    }

    /**
     * 检查是否已存在该产品的待处理库存预警建议
     */
    private boolean existsPendingStockAlertSuggest(Long productId) {
        Long count = erpPurchaseSuggestMapper.selectCount(
                new LambdaQueryWrapper<ErpPurchaseSuggestDO>()
                        .eq(ErpPurchaseSuggestDO::getMaterialId, productId)
                        .in(ErpPurchaseSuggestDO::getStatus,
                                ErpMrpSuggestStatusEnum.TO_CONFIRM.getStatus(),
                                ErpMrpSuggestStatusEnum.CONFIRMED.getStatus())
                        .eq(ErpPurchaseSuggestDO::getRemark, STOCK_ALERT_REMARK));
        return count > 0;
    }

}
