package cn.weitee.erp.module.erp.service.product;

import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ProductSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductPendingChangeDO;

import java.util.Map;
import java.util.Set;

/**
 * 物料待审变更暂存 Service 接口
 *
 * 物料修改审批的暂存表模式：
 * 提交修改时计算 diff 并写入暂存表（主表保持原值）；
 * 审批通过后按快照落主表；驳回/撤回仅处理暂存记录，主表无损。
 */
public interface ErpProductPendingChangeService {

    /**
     * 计算物料修改 diff（主表现值 vs 入参目标值）
     *
     * @return field -> newValue（LinkedHashMap，仅包含发生变化的字段）
     */
    Map<String, Object> diffProduct(ErpProductDO existed, ProductSaveReqVO reqVO);

    /**
     * 关键字段冻结校验：物料被研发/制造 BOM 引用时，
     * 变更字段命中 standard 则抛 PRODUCT_FROZEN_FIELD_LOCKED。
     * materialCode 已通过编码沿革机制（{@link #recordMaterialCodeChange}）放开冻结，可追溯改码。
     *
     * @param productId     物料编号
     * @param changedFields 本次变更的字段名集合
     */
    void validateFrozenFields(Long productId, Set<String> changedFields);

    /**
     * 编码沿革统一入口：旧码占用校验 + 写沿革记录（append-only）。
     * 新码命中其他物料的沿革旧码时抛 PRODUCT_MATERIAL_CODE_HISTORY_OCCUPIED；
     * newCode 为空或与 oldCode 相同视为未改码，直接跳过。
     *
     * @param productId         物料编号
     * @param oldCode           变更前编码
     * @param newCode           变更后编码
     * @param processInstanceId 关联审批流程实例（直改路径为 null）
     * @param reason            变更原因
     */
    void recordMaterialCodeChange(Long productId, String oldCode, String newCode,
                                  String processInstanceId, String reason);

    /**
     * 判断物料是否被任意研发 BOM 或制造 BOM 明细引用
     */
    boolean isReferencedByBom(Long productId);

    /**
     * 批量判断物料是否被 BOM 引用（供列表/详情 VO 填充，避免 N+1）
     *
     * @return 被引用的物料编号集合
     */
    java.util.Set<Long> getReferencedProductIds(java.util.Collection<Long> productIds);

    /**
     * 覆盖式写入暂存记录（delete + insert，同事务内保证唯一约束不冲突）
     *
     * @param productId     物料编号
     * @param target        变更后完整字段快照（序列化入 change_data）
     * @param changedFields 变更字段名集合
     * @param reason        提交说明
     * @return 暂存记录编号
     */
    Long upsertPendingChange(Long productId, ProductSaveReqVO target, Set<String> changedFields, String reason);

    /**
     * 覆盖式写入暂存记录并归组到批量导入批次（单条修改请走 {@link #upsertPendingChange}）
     *
     * @param productId     物料编号
     * @param batchId       批量导入批次编号
     * @param target        变更后完整字段快照
     * @param changedFields 变更字段名集合
     * @param reason        提交说明
     * @return 暂存记录编号
     */
    Long upsertBatchPendingChange(Long productId, Long batchId, ProductSaveReqVO target,
                                  Set<String> changedFields, String reason);

    /**
     * 查询批次的全部暂存记录（批量导入归组）
     */
    java.util.List<ErpProductPendingChangeDO> getPendingChangesByBatch(Long batchId);

    /**
     * 批量查询多个物料的在途暂存记录（列表 VO 批次归组提示用，防 N+1）
     */
    java.util.List<ErpProductPendingChangeDO> getPendingChangesByProductIds(java.util.Collection<Long> productIds);

    /**
     * 统计批次成员数
     */
    Long countPendingChangesByBatch(Long batchId);

    /**
     * BPM 创建失败补偿（批量）：批次内全部暂存记录标记 FAILED（支持整批重新提交）
     */
    void markBatchFailed(Long batchId, String reason);

    /**
     * 回写批次内全部暂存记录的流程实例编号（BPM 创建成功后调用）
     */
    void updateBatchProcessInstanceId(Long batchId, String processInstanceId);

    /**
     * 查询物料的在途暂存记录（含待审/失败），无则返回 null
     */
    ErpProductPendingChangeDO getPendingChange(Long productId);

    /**
     * 审批通过：将暂存快照落至主表，并完结暂存记录。
     * 先业务后终态：落库成功后才更新暂存状态；重复回调幂等跳过。
     *
     * @return true=本次执行了落库；false=重复事件被幂等跳过
     */
    boolean applyPendingChange(Long productId, String processInstanceId, String reason);

    /**
     * 审批驳回：暂存记录标记驳回留痕，主表不动（保持原值）。
     *
     * @return true=本次执行；false=重复事件被幂等跳过
     */
    boolean rejectPendingChange(Long productId, String processInstanceId, String reason);

    /**
     * 发起人撤回：删除暂存记录，主表保持原值
     */
    void discardPendingChange(Long productId);

    /**
     * BPM 创建失败补偿：暂存记录标记 FAILED（支持重新提交）
     */
    void markFailed(Long productId, String reason);

    /**
     * 回写暂存记录的流程实例编号（BPM 创建成功后调用）
     */
    void updateProcessInstanceId(Long productId, String processInstanceId);

    /**
     * 查询物料修改审批视图：主表现值 + 暂存目标值 + 字段级 diff
     *
     * @param productId 物料编号
     * @return 审批视图；无在途变更时 diffs 为空列表、target 为 null
     */
    cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductApprovalViewRespVO getApprovalView(Long productId);

    /**
     * 查询物料批量修改审批视图：批次内全部物料的字段级 diff
     *
     * @param batchId 批量导入批次编号
     * @return 批次审批视图
     */
    cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductBatchApprovalViewRespVO getBatchApprovalView(
            Long batchId);

}
