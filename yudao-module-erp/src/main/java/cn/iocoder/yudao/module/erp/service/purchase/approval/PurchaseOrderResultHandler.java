package cn.iocoder.yudao.module.erp.service.purchase.approval;

import cn.iocoder.yudao.module.bpm.service.approval.handler.ApprovalResultHandler;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseOrderService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_ORDER_NOT_EXISTS;

@Component
public class PurchaseOrderResultHandler implements ApprovalResultHandler {

    @Resource
    private ErpPurchaseOrderMapper purchaseOrderMapper;
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
        purchaseOrderMapper.clearProcessInstanceId(bizId);
    }

    private void validateOrderExists(Long orderId) {
        if (purchaseOrderMapper.selectById(orderId) == null) {
            throw exception(PURCHASE_ORDER_NOT_EXISTS);
        }
    }
}
