package cn.weitee.erp.module.erp.service.sale.approval;

import cn.weitee.erp.module.bpm.service.approval.handler.ApprovalResultHandler;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.service.sale.ErpSaleOrderService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.SALE_ORDER_NOT_EXISTS;

@Component
public class SaleOrderResultHandler implements ApprovalResultHandler {

    @Resource
    private ErpSaleOrderService saleOrderService;

    @Override
    public String getSceneCode() {
        return "erp.sale.order.submit";
    }

    @Override
    public void onApprove(Long bizId, String processInstanceId, String reason) {
        validateExists(bizId);
        saleOrderService.updateSaleOrderStatusByBpm(bizId, processInstanceId,
                ErpAuditStatus.APPROVE.getStatus(), reason);
    }

    @Override
    public void onReject(Long bizId, String processInstanceId, String reason) {
        validateExists(bizId);
        saleOrderService.updateSaleOrderStatusByBpm(bizId, processInstanceId,
                ErpAuditStatus.REJECT.getStatus(), reason);
    }

    @Override
    public void onCancel(Long bizId, String processInstanceId, String reason) {
        validateExists(bizId);
        saleOrderService.rollbackSaleOrderStatusToDraftByBpm(bizId, processInstanceId, reason);
    }

    private void validateExists(Long orderId) {
        if (saleOrderService.getSaleOrder(orderId) == null) {
            throw exception(SALE_ORDER_NOT_EXISTS);
        }
    }
}
