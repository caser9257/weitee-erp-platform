package cn.weitee.erp.module.erp.service.purchase.approval;

import cn.weitee.erp.module.bpm.service.approval.handler.ApprovalResultHandler;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseInService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_NOT_EXISTS;

@Component
public class PurchaseInResultHandler implements ApprovalResultHandler {

    @Resource
    private ErpPurchaseInMapper purchaseInMapper;
    @Resource
    private ErpPurchaseInService purchaseInService;

    @Override
    public String getSceneCode() {
        return "erp.purchase.in.submit";
    }

    @Override
    public void onApprove(Long bizId, String processInstanceId, String reason) {
        validateExists(bizId);
        purchaseInService.updatePurchaseInStatusByBpm(bizId, processInstanceId,
                ErpAuditStatus.APPROVE.getStatus(), reason);
    }

    @Override
    public void onReject(Long bizId, String processInstanceId, String reason) {
        validateExists(bizId);
        purchaseInService.updatePurchaseInStatusByBpm(bizId, processInstanceId,
                ErpAuditStatus.REJECT.getStatus(), reason);
    }

    @Override
    public void onCancel(Long bizId, String processInstanceId, String reason) {
        purchaseInMapper.clearProcessInstanceId(bizId);
    }

    private void validateExists(Long purchaseInId) {
        if (purchaseInMapper.selectById(purchaseInId) == null) {
            throw exception(PURCHASE_IN_NOT_EXISTS);
        }
    }
}
