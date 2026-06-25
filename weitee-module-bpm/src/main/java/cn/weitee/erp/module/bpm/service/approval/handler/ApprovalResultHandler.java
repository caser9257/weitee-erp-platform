package cn.weitee.erp.module.bpm.service.approval.handler;

/**
 * 审批结果处理器接口
 *
 * 业务模块需要实现此接口，处理审批通过、驳回、撤回的结果
 */
public interface ApprovalResultHandler {

    /**
     * 返回此 Handler 支持的场景编码
     *
     * @return 场景编码，如 erp.finance.payment.submit
     */
    String getSceneCode();

    /**
     * 审批通过
     *
     * @param bizId            业务单据 ID
     * @param processInstanceId 流程实例 ID
     * @param reason           审批原因
     */
    void onApprove(Long bizId, String processInstanceId, String reason);

    /**
     * 审批驳回
     *
     * @param bizId            业务单据 ID
     * @param processInstanceId 流程实例 ID
     * @param reason           驳回原因
     */
    void onReject(Long bizId, String processInstanceId, String reason);

    /**
     * 审批撤回
     *
     * @param bizId            业务单据 ID
     * @param processInstanceId 流程实例 ID
     * @param reason           撤回原因
     */
    void onCancel(Long bizId, String processInstanceId, String reason);

}
