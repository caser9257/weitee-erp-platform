package cn.weitee.erp.module.erp.service.stock.approval;

import cn.weitee.erp.module.bpm.service.approval.handler.SnapshotAwareApprovalResultHandler;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpStockMoveBpmConstants;
import cn.weitee.erp.module.erp.service.stock.ErpStockMoveService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Component
public class StockMoveResultHandler implements SnapshotAwareApprovalResultHandler {

    @Resource
    private ErpStockMoveService stockMoveService;

    @Override
    public String getSceneCode() {
        return ErpStockMoveBpmConstants.SCENE_CODE;
    }

    @Override
    public void onApprove(Long bizId, String processInstanceId, String reason) {
        stockMoveService.updateStockMoveStatusByBpm(bizId, processInstanceId, ErpAuditStatus.APPROVE.getStatus(), reason);
    }

    @Override
    public void onApproveWithSnapshot(Long bizId, String processInstanceId, String snapshotId, String reason) {
        onApprove(bizId, processInstanceId, reason);
    }

    @Override
    public void onReject(Long bizId, String processInstanceId, String reason) {
        stockMoveService.rollbackStockMoveStatusToDraftByBpm(bizId, processInstanceId, reason);
    }

    @Override
    public void onRejectWithSnapshot(Long bizId, String processInstanceId, String snapshotId, String reason) {
        onReject(bizId, processInstanceId, reason);
    }

    @Override
    public void onCancel(Long bizId, String processInstanceId, String reason) {
        stockMoveService.rollbackStockMoveStatusToDraftByBpm(bizId, processInstanceId, reason);
    }

    @Override
    public void onCancelWithSnapshot(Long bizId, String processInstanceId, String snapshotId, String reason) {
        onCancel(bizId, processInstanceId, reason);
    }
}
