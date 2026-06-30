package cn.weitee.erp.module.bpm.service.approval.handler;

/**
 * 支持 snapshot 绑定校验的审批结果处理器。
 *
 * 仅在需要区分“真实 BPM processInstanceId”和“当前业务单据绑定的 snapshotId”时使用，
 * 例如供应链模块为防止旧快照延迟回调误伤新一轮审批中的单据。
 */
public interface SnapshotAwareApprovalResultHandler extends ApprovalResultHandler {

    void onApproveWithSnapshot(Long bizId, String processInstanceId, String snapshotId, String reason);

    void onRejectWithSnapshot(Long bizId, String processInstanceId, String snapshotId, String reason);

    void onCancelWithSnapshot(Long bizId, String processInstanceId, String snapshotId, String reason);

    @Override
    default void onApprove(Long bizId, String processInstanceId, String reason) {
        onApproveWithSnapshot(bizId, processInstanceId, processInstanceId, reason);
    }

    @Override
    default void onReject(Long bizId, String processInstanceId, String reason) {
        onRejectWithSnapshot(bizId, processInstanceId, processInstanceId, reason);
    }

    @Override
    default void onCancel(Long bizId, String processInstanceId, String reason) {
        onCancelWithSnapshot(bizId, processInstanceId, processInstanceId, reason);
    }
}
