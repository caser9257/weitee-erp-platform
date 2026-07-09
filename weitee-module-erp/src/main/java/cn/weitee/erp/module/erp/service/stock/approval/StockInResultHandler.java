package cn.weitee.erp.module.erp.service.stock.approval;

import cn.weitee.erp.module.bpm.service.approval.handler.ApprovalResultHandler;
import cn.weitee.erp.module.bpm.service.approval.handler.SnapshotAwareApprovalResultHandler;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.service.stock.ErpStockInService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.STOCK_IN_NOT_EXISTS;

@Component
public class StockInResultHandler implements SnapshotAwareApprovalResultHandler {

    @Resource
    private ErpStockInService stockInService;

    @Override
    public String getSceneCode() {
        return "erp.stock.in.submit";
    }

    @Override
    public void onApprove(Long bizId, String processInstanceId, String reason) {
        validateExists(bizId);
        stockInService.updateStockInStatusByBpm(bizId, processInstanceId,
                ErpAuditStatus.APPROVE.getStatus(), reason);
    }

    @Override
    public void onApproveWithSnapshot(Long bizId, String processInstanceId, String snapshotId, String reason) {
        validateExists(bizId);
        stockInService.updateStockInStatusByBpm(bizId, processInstanceId,
                ErpAuditStatus.APPROVE.getStatus(), reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onReject(Long bizId, String processInstanceId, String reason) {
        stockInService.rollbackStockInStatusToDraftByBpm(bizId, processInstanceId, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onRejectWithSnapshot(Long bizId, String processInstanceId, String snapshotId, String reason) {
        stockInService.rollbackStockInStatusToDraftByBpm(bizId, processInstanceId, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onCancel(Long bizId, String processInstanceId, String reason) {
        stockInService.rollbackStockInStatusToDraftByBpm(bizId, processInstanceId, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onCancelWithSnapshot(Long bizId, String processInstanceId, String snapshotId, String reason) {
        stockInService.rollbackStockInStatusToDraftByBpm(bizId, processInstanceId, reason);
    }

    private void validateExists(Long stockInId) {
        if (stockInService.getStockIn(stockInId) == null) {
            throw exception(STOCK_IN_NOT_EXISTS);
        }
    }
}
