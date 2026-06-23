package cn.iocoder.yudao.module.erp.framework.listener;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpPurchaseSuggestDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpPurchaseSuggestMapper;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpMrpSuggestStatusEnum;
import cn.iocoder.yudao.module.erp.framework.event.PurchaseOrderChangedEvent;
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
     * 处理数量变更：标记关联建议待重新计算
     *
     * TODO: 当前仅添加标记，后续接入 MRP 重算逻辑时完善。
     *       需根据变更后的数量差值调整建议数量或触发 MRP 重算。
     */
    private void handleQuantityChanged(Long purchaseOrderId) {
        List<ErpPurchaseSuggestDO> suggests = getConvertedSuggestsByOrderId(purchaseOrderId);
        if (CollUtil.isEmpty(suggests)) {
            return;
        }
        // TODO: 实现数量同步逻辑（根据采购订单变更后数量与建议数量的差值进行修正）
        log.info("[handleQuantityChanged] 待实现：数量同步，purchaseOrderId={}, suggestCount={}", purchaseOrderId, suggests.size());
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
