package cn.weitee.erp.module.erp.service.purchase.approval;

import cn.weitee.erp.module.bpm.service.approval.handler.ApprovalResultHandler;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseOrderService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_ORDER_NOT_EXISTS;

@Component
public class PurchaseOrderResultHandler implements ApprovalResultHandler {

    @Resource
    private ErpPurchaseOrderService purchaseOrderService;

    @Override
    public String getSceneCode() {
        return "erp.purchase.order.submit";
    }

    @Override
    public void onApprove(Long bizId, String processInstanceId, String reason) {
        validateOrderExists(bizId);
        purchaseOrderService.updatePurchaseOrderStatusByBpm(bizId, processInstanceId,
                ErpAuditStatus.APPROVE.getStatus(), reason);
    }

    @Override
    public void onReject(Long bizId, String processInstanceId, String reason) {
        validateOrderExists(bizId);
        purchaseOrderService.updatePurchaseOrderStatusByBpm(bizId, processInstanceId,
                ErpAuditStatus.REJECT.getStatus(), reason);
    }

    @Override
    public void onCancel(Long bizId, String processInstanceId, String reason) {
        validateOrderExists(bizId);
        purchaseOrderService.rollbackPurchaseOrderStatusToDraftByBpm(bizId, processInstanceId, reason);
    }

    private void validateOrderExists(Long orderId) {
        if (purchaseOrderService.getPurchaseOrder(orderId) == null) {
            throw exception(PURCHASE_ORDER_NOT_EXISTS);
        }
    }
}
