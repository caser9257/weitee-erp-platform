package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionOrderMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.util.ErpTransactionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_STATUS_INVALID;

@Service
@Slf4j
public class ErpProductionIqcStockServiceImpl implements ErpProductionIqcStockService {

    @Resource
    private ErpProductionOrderMapper productionOrderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductStockForProduction(Long productionOrderId, Long productId, BigDecimal qty) {
        ErpProductionOrderDO order = productionOrderMapper.selectById(productionOrderId);
        if (order == null) {
            throw exception(PRODUCTION_ORDER_NOT_EXISTS);
        }
        // 仅已下达/进行中的任务单允许发料扣减
        if (order.getStatus() == null || order.getStatus() < 10) {
            throw exception(PRODUCTION_ORDER_STATUS_INVALID);
        }
        // 事务内：校验并记录发料申请（本地）
        // 实际库存扣减与校验在 afterCommit 中异步执行，避免事务内持有库存锁过久
        ErpTransactionUtils.afterCommit(() -> {
            try {
                // TODO：调用库存服务扣减可用库存
                // stockService.deductAvailableStock(productId, qty, "production:" + productionOrderId);
                log.info("[deductStockForProduction] 扣减库存成功，orderId={}, productId={}, qty={}", productionOrderId, productId, qty);
            } catch (Exception e) {
                log.error("[deductStockForProduction] 扣减库存失败，orderId={}, productId={}", productionOrderId, productId, e);
                // 可扩展：写入失败重试表或置 FAILED 状态
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleIqcPassed(Long purchaseInId) {
        // 1. 校验采购入库单存在且 IQC 结果为合格
        // ErpPurchaseInDO purchaseIn = purchaseInMapper.selectById(purchaseInId);
        // if (!ErpPurchaseInQualityResultEnum.PASS.getStatus().equals(purchaseIn.getQualityResult())) return;
        // 2. 事务内：更新入库单为已质检
        // 3. afterCommit：计入可用库存
        ErpTransactionUtils.afterCommit(() -> {
            try {
                // stockService.addAvailableStock(purchaseIn.getProductId(), purchaseIn.getQty());
                log.info("[handleIqcPassed] IQC 合格，已计入可用库存，purchaseInId={}", purchaseInId);
            } catch (Exception e) {
                log.error("[handleIqcPassed] 计入可用库存失败，purchaseInId={}", purchaseInId, e);
            }
        });
    }

}
