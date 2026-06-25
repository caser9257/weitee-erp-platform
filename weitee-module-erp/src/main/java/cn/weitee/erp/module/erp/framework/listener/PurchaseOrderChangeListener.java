package cn.weitee.erp.module.erp.framework.listener;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.util.collection.CollectionUtils;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpPurchaseSuggestDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpPurchaseSuggestMapper;
import cn.weitee.erp.module.erp.enums.mrp.ErpMrpSuggestStatusEnum;
import cn.weitee.erp.module.erp.framework.event.PurchaseOrderChangedEvent;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 采购订单变更监听器
 *
 * 使用 @TransactionalEventListener 确保主事务提交后才执行。
 */
@Component
@Slf4j
public class PurchaseOrderChangeListener {

    @Resource
    private ErpPurchaseSuggestMapper erpPurchaseSuggestMapper;

    @Async
    @Transactional(rollbackFor = Exception.class)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPurchaseOrderChanged(PurchaseOrderChangedEvent event) {
        try {
            log.info("[onPurchaseOrderChanged] purchaseOrderId={}, changeType={}", event.getPurchaseOrderId(), event.getChangeType());
            switch (event.getChangeType()) {
                case QUANTITY_CHANGED:
                    handleQuantityChanged(event.getPurchaseOrderId());
                    break;
                case ORDER_CANCELLED:
                case ORDER_REJECTED:
                    handleOrderCancelled(event.getPurchaseOrderId());
                    break;
                default:
                    log.warn("[onPurchaseOrderChanged] 未知变更类型：{}", event.getChangeType());
            }
        } catch (Exception e) {
            log.error("[onPurchaseOrderChanged] 处理失败，purchaseOrderId={}", event.getPurchaseOrderId(), e);
        }
    }

    /**
     * 处理数量变更：将关联建议回退至「待确认」状态，清除采购订单关联
     *
     * 数量变更后，原有建议数量已不准确，需回退至待确认状态让用户重新审核。
     * 与 handleOrderCancelled 的区别：数量变更时保留建议，仅重置状态。
     */
    private void handleQuantityChanged(Long purchaseOrderId) {
        List<ErpPurchaseSuggestDO> suggests = getConvertedSuggestsByOrderId(purchaseOrderId);
        if (CollUtil.isEmpty(suggests)) {
            return;
        }

        List<Long> ids = CollectionUtils.convertList(suggests, ErpPurchaseSuggestDO::getId);

        // 批量更新：回退状态至待确认，清除采购订单关联
        erpPurchaseSuggestMapper.update(null,
                new LambdaUpdateWrapper<ErpPurchaseSuggestDO>()
                        .in(ErpPurchaseSuggestDO::getId, ids)
                        .set(ErpPurchaseSuggestDO::getStatus, ErpMrpSuggestStatusEnum.TO_CONFIRM.getStatus())
                        .set(ErpPurchaseSuggestDO::getConvertPurchaseOrderId, null));

        log.info("[handleQuantityChanged] 已回退 {} 条建议至待确认，purchaseOrderId={}", ids.size(), purchaseOrderId);
    }

    /**
     * 处理订单取消/反审核：回退关联建议状态至「已确认」并清除关联订单
     * 使用批量 UPDATE 避免 N+1 问题
     */
    private void handleOrderCancelled(Long purchaseOrderId) {
        List<ErpPurchaseSuggestDO> suggests = getConvertedSuggestsByOrderId(purchaseOrderId);
        if (CollUtil.isEmpty(suggests)) {
            log.info("[handleOrderCancelled] 无关联建议，purchaseOrderId={}", purchaseOrderId);
            return;
        }

        List<Long> ids = CollectionUtils.convertList(suggests, ErpPurchaseSuggestDO::getId);

        // 批量更新，避免 N+1
        erpPurchaseSuggestMapper.update(null,
                new LambdaUpdateWrapper<ErpPurchaseSuggestDO>()
                        .in(ErpPurchaseSuggestDO::getId, ids)
                        .set(ErpPurchaseSuggestDO::getStatus, ErpMrpSuggestStatusEnum.CONFIRMED.getStatus())
                        .set(ErpPurchaseSuggestDO::getConvertPurchaseOrderId, null));

        log.info("[handleOrderCancelled] 已回退 {} 条建议，purchaseOrderId={}", ids.size(), purchaseOrderId);
    }

    private List<ErpPurchaseSuggestDO> getConvertedSuggestsByOrderId(Long purchaseOrderId) {
        return erpPurchaseSuggestMapper.selectList(
                new LambdaQueryWrapper<ErpPurchaseSuggestDO>()
                        .eq(ErpPurchaseSuggestDO::getConvertPurchaseOrderId, purchaseOrderId)
                        .eq(ErpPurchaseSuggestDO::getStatus, ErpMrpSuggestStatusEnum.CONVERTED.getStatus()));
    }

}
