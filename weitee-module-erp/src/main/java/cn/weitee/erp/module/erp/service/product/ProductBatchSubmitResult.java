package cn.weitee.erp.module.erp.service.product;

/**
 * 物料批量修改审批提交结果
 *
 * afterCommit 中 BPM 创建同步完成后回填结果（提交方法返回时结果已确定），
 * 调用方据此决定发送"已提交批量审批"统计通知还是跳过（失败通知已由 Service 发出）。
 *
 * @param batchId         批次编号
 * @param bpmCreateFailed BPM 流程创建是否失败（失败时整批已标记 FAILED、物料状态已回滚）
 * @param failReason      失败原因（bpmCreateFailed=true 时有值）
 */
public record ProductBatchSubmitResult(Long batchId, boolean bpmCreateFailed, String failReason) {

    public static ProductBatchSubmitResult ok(Long batchId) {
        return new ProductBatchSubmitResult(batchId, false, null);
    }

    public static ProductBatchSubmitResult failed(Long batchId, String failReason) {
        return new ProductBatchSubmitResult(batchId, true, failReason);
    }

}
