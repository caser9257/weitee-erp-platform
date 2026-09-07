package cn.weitee.erp.module.erp.service.product;

import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ProductSaveReqVO;

public interface ErpProductBpmService {

    String submitProduct(Long userId, Long productId);

    /**
     * 提交物料修改审批（暂存表模式）
     *
     * 事务内：防重校验 → diff 计算 → 冻结校验 → 写暂存 → 主表 CAS 锁 PROCESS；
     * afterCommit：创建 BPM 流程，成功回写流程实例编号，失败标记暂存 FAILED 并回滚主表状态。
     *
     * @return 暂存变更编号
     */
    Long submitProductUpdate(Long userId, ProductSaveReqVO updateReqVO);

    void cancelProductApproval(Long userId, Long productId, String reason);

    /**
     * 两段式变更阶段一：发起变更申请（APPROVE → CR_PENDING）
     *
     * 事务内：审批流守卫 → CAS 锁 CR_PENDING；
     * afterCommit：创建 BPM 流程（SCENE_CODE_CHANGE_REQUEST），失败标记 FAILED 并回滚 APPROVE。
     *
     * @param userId     操作人
     * @param productId  物料编号
     * @param reason     变更申请原因
     */
    void submitChangeRequest(Long userId, Long productId, String reason);

    /**
     * 两段式变更阶段二：提交变更完成确认（EDITING → CONFIRM_PENDING）
     *
     * 事务内：审批流守卫 → 暂存快照完整性校验 → CAS 锁 CONFIRM_PENDING；
     * afterCommit：创建 BPM 流程（SCENE_CODE_CHANGE_CONFIRM），失败标记 FAILED 并回滚 EDITING。
     *
     * @param userId    操作人
     * @param productId 物料编号
     * @param reason    变更完成说明
     */
    void submitChangeConfirm(Long userId, Long productId, String reason);

    /**
     * 废除一段式：发起废除申请（APPROVE → OBSOLETE_CR_PENDING）
     *
     * 废除原因必填，发起时预写主表 abolish_reason/abolish_by（最终留痕）。
     * 事务内：审批流守卫 → 预写留痕 → CAS 锁 OBSOLETE_CR_PENDING；
     * afterCommit：创建 BPM 流程（SCENE_CODE_OBSOLETE_REQUEST），失败标记 FAILED 并回滚 APPROVE。
     *
     * @param userId    操作人
     * @param productId 物料编号
     * @param reason    废除原因（最终留痕）
     */
    void submitObsoleteRequest(Long userId, Long productId, String reason);

    /**
     * 启停一段式：发起启停审批（APPROVE → STOP_PENDING）
     *
     * 目标 status 写入暂存快照（审批通过才落主表，审批期间状态不变）。
     * 事务内：审批流守卫 → 写暂存 → CAS 锁 STOP_PENDING；
     * afterCommit：创建 BPM 流程（SCENE_CODE_STATUS_CHANGE），失败标记 FAILED 并回滚 APPROVE。
     *
     * @param userId      操作人
     * @param productId   物料编号
     * @param targetStatus 目标状态（CommonStatusEnum.ENABLE/DISABLE）
     * @param reason      启停理由
     */
    void submitStatusChange(Long userId, Long productId, Integer targetStatus, String reason);

    /**
     * 撤回两段式审批（变更阶段一/阶段二、废除阶段一/阶段二通用）
     *
     * 按当前 auditStatus 分流撤回对应场景码；终态回滚统一由对应 ResultHandler.onCancel 处理。
     *
     * @param userId    操作人
     * @param productId 物料编号
     * @param reason    撤回原因
     */
    void cancelTwoStageApproval(Long userId, Long productId, String reason);

}
