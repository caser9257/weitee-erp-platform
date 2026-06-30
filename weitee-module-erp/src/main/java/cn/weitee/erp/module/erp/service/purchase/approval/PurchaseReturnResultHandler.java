package cn.weitee.erp.module.erp.service.purchase.approval;

import cn.weitee.erp.module.bpm.service.approval.handler.ApprovalResultHandler;
import cn.weitee.erp.module.bpm.service.approval.handler.SnapshotAwareApprovalResultHandler;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseReturnService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_RETURN_NOT_EXISTS;

@Component
public class PurchaseReturnResultHandler implements SnapshotAwareApprovalResultHandler {

    @Resource
    private ErpPurchaseReturnService purchaseReturnService;

    @Override
    public String getSceneCode() {
        return "erp.purchase.return.submit";
    }

    @Override
    public void onApprove(Long bizId, String processInstanceId, String reason) {
        validateExists(bizId);
        purchaseReturnService.updatePurchaseReturnStatusByBpm(bizId, processInstanceId,
                ErpAuditStatus.APPROVE.getStatus(), reason);
    }

    @Override
    public void onApproveWithSnapshot(Long bizId, String processInstanceId, String snapshotId, String reason) {
        validateExists(bizId);
        purchaseReturnService.updatePurchaseReturnStatusByBpm(bizId, snapshotId,
                ErpAuditStatus.APPROVE.getStatus(), reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onReject(Long bizId, String processInstanceId, String reason) {
        purchaseReturnService.rollbackPurchaseReturnStatusToDraftByBpm(bizId, processInstanceId, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onRejectWithSnapshot(Long bizId, String processInstanceId, String snapshotId, String reason) {
        purchaseReturnService.rollbackPurchaseReturnStatusToDraftByBpm(bizId, snapshotId, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onCancel(Long bizId, String processInstanceId, String reason) {
        purchaseReturnService.rollbackPurchaseReturnStatusToDraftByBpm(bizId, processInstanceId, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onCancelWithSnapshot(Long bizId, String processInstanceId, String snapshotId, String reason) {
        purchaseReturnService.rollbackPurchaseReturnStatusToDraftByBpm(bizId, snapshotId, reason);
    }

    private void validateExists(Long purchaseReturnId) {
        if (purchaseReturnService.getPurchaseReturn(purchaseReturnId) == null) {
            throw exception(PURCHASE_RETURN_NOT_EXISTS);
        }
    }
}
