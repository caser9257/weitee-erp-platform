package cn.weitee.erp.module.erp.service.product;

import java.util.List;

/**
 * 物料批量修改审批 Service
 *
 * 一次批量导入（如 Cadence 物料维护）合并为一个批次审批（bizId = batchId）：
 * - 提交：事务内写入暂存变更（batch_id 归组）并 CAS 锁定全部物料；
 * - 通过：整批暂存变更落主表；驳回/撤回：整批作废，主表无损；
 * - BPM 创建失败：整批标记 FAILED、物料状态回滚，并发送失败站内信。
 */
public interface ErpProductBatchUpdateBpmService {

    /**
     * 提交批量修改审批
     *
     * @param userId            操作人
     * @param items             批量变更明细（导入校验阶段已逐行完成 diff 与冻结校验）
     * @param sceneDescription  业务场景描述（用于失败站内信，如 "Cadence 数据导入"）
     * @return 提交结果（含 BPM 创建是否失败；失败时整批已标记 FAILED、物料状态已回滚、失败通知已发送）
     */
    ProductBatchSubmitResult submitBatchUpdate(Long userId, List<ProductBatchUpdateItem> items, String sceneDescription);

}
