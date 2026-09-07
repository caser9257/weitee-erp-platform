package cn.weitee.erp.module.erp.service.product;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ProductSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductPendingChangeDO;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpProductBpmConstants;
import cn.weitee.erp.module.erp.util.ErpTransactionUtils;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_APPROVE_FAIL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_AUDIT_STATUS_ILLEGAL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_BPM_CANCEL_FAIL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_BPM_SUBMIT_FAIL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_NO_CHANGES;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_OBSOLETE_REASON_REQUIRED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_UPDATE_PROCESSING;

@Service
@Validated
@Slf4j
public class ErpProductBpmServiceImpl implements ErpProductBpmService {

    @Resource
    private ErpProductMapper productMapper;
    @Resource
    private BpmApprovalRuntimeService approvalRuntimeService;
    @Resource
    private ErpProductPendingChangeService pendingChangeService;
    @Resource
    private ErpProductService productService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitProduct(Long userId, Long productId) {
        ErpProductDO product = getRequiredProduct(productId);
        Integer auditStatus = product.getAuditStatus() != null ? product.getAuditStatus() : ErpAuditStatus.DRAFT.getStatus();
        if (ObjectUtil.equal(auditStatus, ErpAuditStatus.APPROVE.getStatus())) {
            throw exception(PRODUCT_APPROVE_FAIL);
        }
        if (ObjectUtil.equal(auditStatus, ErpAuditStatus.PROCESS.getStatus())
                && StrUtil.isNotBlank(product.getProcessInstanceId())) {
            throw exception(PRODUCT_BPM_SUBMIT_FAIL);
        }
        productMapper.updateById(new ErpProductDO()
                .setId(productId)
                .setAuditStatus(ErpAuditStatus.PROCESS.getStatus())
                .setProcessInstanceId(null));
        ErpTransactionUtils.afterCommit(() -> {
            try {
                String processInstanceId = approvalRuntimeService.submit(
                        ErpProductBpmConstants.SCENE_CODE, productId, userId);
                productMapper.updateById(new ErpProductDO()
                        .setId(productId)
                        .setProcessInstanceId(processInstanceId));
            } catch (Exception e) {
                log.error("[submitProduct] BPM 创建失败，productId={}", productId, e);
                productMapper.updateById(new ErpProductDO()
                        .setId(productId)
                        .setAuditStatus(ErpAuditStatus.FAILED.getStatus())
                        .setProcessInstanceId(null));
            }
        });
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitProductUpdate(Long userId, ProductSaveReqVO updateReqVO) {
        Long productId = updateReqVO.getId();
        ErpProductDO product = getRequiredProduct(productId);
        Integer auditStatus = product.getAuditStatus() != null ? product.getAuditStatus() : ErpAuditStatus.DRAFT.getStatus();
        // 1. 状态路由：
        //    - 两段式 EDITING：保存到暂存快照（可反复保存），主表保持基线，不创建流程；
        //      保存与提交分离，由发起人修改完成后手动「提交审批」触发阶段二
        //    - 未生效物料（草稿/驳回/流程失败）编辑无需审批，直接落库；
        //    - 生效物料（APPROVE）编辑必须走修改审批。审批中一律拒绝。
        if (ErpAuditStatus.EDITING.getStatus().equals(auditStatus)) {
            var editChanges = pendingChangeService.diffProduct(product, updateReqVO);
            if (editChanges.isEmpty()) {
                throw exception(PRODUCT_NO_CHANGES);
            }
            pendingChangeService.validateFrozenFields(productId, editChanges.keySet());
            pendingChangeService.upsertPendingChange(productId, updateReqVO, editChanges.keySet(), null);
            return null;
        }
        if (!ErpAuditStatus.APPROVE.getStatus().equals(auditStatus)) {
            productService.updateProduct(updateReqVO);
            return null;
        }
        if (StrUtil.isNotBlank(product.getProcessInstanceId())
                && ObjectUtil.equal(auditStatus, ErpAuditStatus.PROCESS.getStatus())) {
            throw exception(PRODUCT_UPDATE_PROCESSING, product.getName());
        }
        // 2. diff 计算：无变更直接拒绝，避免空审批
        var changes = pendingChangeService.diffProduct(product, updateReqVO);
        if (changes.isEmpty()) {
            throw exception(PRODUCT_NO_CHANGES);
        }
        // 3. 关键字段冻结校验：被 BOM 引用的物料禁改 materialCode/standard（根因拦截，非前端禁用）
        pendingChangeService.validateFrozenFields(productId, changes.keySet());
        // 4. 写暂存（delete + insert 覆盖在途记录）
        Long pendingId = pendingChangeService.upsertPendingChange(productId, updateReqVO,
                changes.keySet(), null);
        // 5. 主表 CAS 锁定 PROCESS（WHERE 含旧状态，并发提交只有一个成功）
        int count = productMapper.update(null, new LambdaUpdateWrapper<ErpProductDO>()
                .eq(ErpProductDO::getId, productId)
                .eq(ErpProductDO::getAuditStatus, ErpAuditStatus.APPROVE.getStatus())
                .set(ErpProductDO::getAuditStatus, ErpAuditStatus.PROCESS.getStatus()));
        if (count == 0) {
            throw exception(PRODUCT_AUDIT_STATUS_ILLEGAL);
        }
        // 6. 事务外创建 BPM 流程；失败标记暂存 FAILED 并回滚主表状态，支持重新提交
        ErpTransactionUtils.afterCommit(() -> {
            try {
                String processInstanceId = approvalRuntimeService.submit(
                        ErpProductBpmConstants.SCENE_CODE_UPDATE, productId, userId);
                pendingChangeService.updateProcessInstanceId(productId, processInstanceId);
                productMapper.update(null, new LambdaUpdateWrapper<ErpProductDO>()
                        .eq(ErpProductDO::getId, productId)
                        .eq(ErpProductDO::getAuditStatus, ErpAuditStatus.PROCESS.getStatus())
                        .set(ErpProductDO::getProcessInstanceId, processInstanceId));
            } catch (Exception e) {
                log.error("[submitProductUpdate] BPM 创建失败，productId={}", productId, e);
                pendingChangeService.markFailed(productId, "流程创建失败：" + e.getMessage());
                rollbackProductToApprove(productId);
                // Finding 1：流程创建成功但后续绑定失败时，立即终止孤儿流程
                try {
                    approvalRuntimeService.cancel(
                            ErpProductBpmConstants.SCENE_CODE_UPDATE, productId,
                            userId,
                            "流程创建后绑定失败，自动终止孤儿流程");
                } catch (Exception cancelEx) {
                    log.warn("[submitProductUpdate] 孤儿流程撤回失败，需人工清理，productId={}, error={}",
                            productId, cancelEx.getMessage());
                }
            }
        });
        return pendingId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelProductApproval(Long userId, Long productId, String reason) {
        ErpProductDO product = getRequiredProduct(productId);
        Integer auditStatus = product.getAuditStatus() != null ? product.getAuditStatus() : ErpAuditStatus.DRAFT.getStatus();
        if (!ObjectUtil.equal(auditStatus, ErpAuditStatus.PROCESS.getStatus())
                || StrUtil.isBlank(product.getProcessInstanceId())) {
            throw exception(PRODUCT_BPM_CANCEL_FAIL);
        }
        // BPM 底层要求取消原因必填：入口兜底默认值，避免空原因导致撤回校验失败
        String safeReason = StrUtil.isNotBlank(reason) ? reason : "发起人撤回";
        // 按在途暂存记录分流场景码（单一路径：终态回滚统一由对应 ResultHandler.onCancel 处理）：
        // - 批量归组（batchId 非空）= 批量导入批次审批，撤回单成员将作废整批变更；
        // - 非归组在途暂存 = 单条修改审批；
        // - 无在途暂存 = 新建审批。
        var pending = pendingChangeService.getPendingChange(productId);
        String sceneCode;
        Long bizId = productId;
        if (pending != null && pending.getBatchId() != null) {
            sceneCode = ErpProductBpmConstants.SCENE_CODE_UPDATE_BATCH;
            bizId = pending.getBatchId();
        } else if (pending != null) {
            sceneCode = ErpProductBpmConstants.SCENE_CODE_UPDATE;
        } else {
            sceneCode = ErpProductBpmConstants.SCENE_CODE;
        }
        // lambda 捕获要求事实终态变量
        final String cancelSceneCode = sceneCode;
        final Long cancelBizId = bizId;
        ErpTransactionUtils.afterCommit(() -> {
            try {
                approvalRuntimeService.cancel(cancelSceneCode, cancelBizId, userId, safeReason);
            } catch (Exception e) {
                log.warn("[cancelProductApproval] BPM 撤回失败，productId={}, sceneCode={}, bizId={}",
                        productId, cancelSceneCode, cancelBizId, e);
                throw e;
            }
        });
    }

    private void rollbackProductToApprove(Long productId) {
        try {
            productMapper.update(null, new LambdaUpdateWrapper<ErpProductDO>()
                    .eq(ErpProductDO::getId, productId)
                    .eq(ErpProductDO::getAuditStatus, ErpAuditStatus.PROCESS.getStatus())
                    .set(ErpProductDO::getAuditStatus, ErpAuditStatus.APPROVE.getStatus()));
        } catch (Exception ex) {
            log.error("[submitProductUpdate] 主表状态回滚失败，productId={}", productId, ex);
        }
    }

    // ========== 两段式变更/废除 ==========

    private void rollbackProductToStage(Long productId, Integer targetStatus) {
        try {
            productMapper.update(null, new LambdaUpdateWrapper<ErpProductDO>()
                    .eq(ErpProductDO::getId, productId)
                    .set(ErpProductDO::getAuditStatus, targetStatus));
        } catch (Exception ex) {
            log.error("[rollbackProductToStage] 主表状态回滚失败，productId={}, target={}", productId, targetStatus, ex);
        }
    }

    /**
     * 通用两段式阶段提交：
     * 1. 审批流守卫（禁止审批流中流转）
     * 2. CAS 锁目标 PENDING 态
     * 3. afterCommit 创建 BPM 流程
     * 4. 失败回滚到源状态
     */
    private void submitTwoStageChange(Long userId, Long productId, Integer expectStatus,
                                       Integer targetPendingStatus, Integer rollbackStatus,
                                       String sceneCode, String stageLabel) {
        ErpProductDO product = getRequiredProduct(productId);
        Integer currentStatus = product.getAuditStatus() != null ? product.getAuditStatus() : ErpAuditStatus.DRAFT.getStatus();
        // 审批流守卫：审批流中禁止流转
        if (ErpAuditStatus.isInApprovalFlow(currentStatus)) {
            throw exception(PRODUCT_BPM_SUBMIT_FAIL, "物料处于" + ErpAuditStatus.nameOf(currentStatus) + "，禁止重复提交");
        }
        if (!expectStatus.equals(currentStatus)) {
            throw exception(PRODUCT_AUDIT_STATUS_ILLEGAL);
        }
        // CAS 锁
        int count = productMapper.update(null, new LambdaUpdateWrapper<ErpProductDO>()
                .eq(ErpProductDO::getId, productId)
                .eq(ErpProductDO::getAuditStatus, expectStatus)
                .set(ErpProductDO::getAuditStatus, targetPendingStatus)
                .set(ErpProductDO::getProcessInstanceId, null));
        if (count == 0) {
            throw exception(PRODUCT_AUDIT_STATUS_ILLEGAL);
        }
        ErpTransactionUtils.afterCommit(() -> {
            try {
                String processInstanceId = approvalRuntimeService.submit(sceneCode, productId, userId);
                productMapper.update(null, new LambdaUpdateWrapper<ErpProductDO>()
                        .eq(ErpProductDO::getId, productId)
                        .eq(ErpProductDO::getAuditStatus, targetPendingStatus)
                        .set(ErpProductDO::getProcessInstanceId, processInstanceId));
            } catch (Exception e) {
                log.error("[submitTwoStageChange][{}] BPM 创建失败，productId={}", stageLabel, productId, e);
                rollbackProductToStage(productId, rollbackStatus);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitChangeRequest(Long userId, Long productId, String reason) {
        submitTwoStageChange(userId, productId,
                ErpAuditStatus.APPROVE.getStatus(),
                ErpAuditStatus.CR_PENDING.getStatus(),
                ErpAuditStatus.APPROVE.getStatus(),
                ErpProductBpmConstants.SCENE_CODE_CHANGE_REQUEST,
                "变更阶段一");
        // 申请理由预写暂存（审批视图展示"申请理由"用）；阶段一无变更内容，changeData 为空 VO。
        // 置于 CAS 成功之后：守卫拒绝时不留任何写入，避免污染其他在途流程的暂存数据
        ProductSaveReqVO emptyReqVO = new ProductSaveReqVO();
        emptyReqVO.setId(productId);
        pendingChangeService.upsertPendingChange(productId, emptyReqVO,
                new java.util.LinkedHashSet<>(), StrUtil.isNotBlank(reason) ? reason : null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitChangeConfirm(Long userId, Long productId, String reason) {
        // 先校验暂存快照存在（没有快照的内容无法确认）
        var pending = pendingChangeService.getPendingChange(productId);
        if (pending == null || !ErpProductPendingChangeDO.STATUS_PENDING.equals(pending.getStatus())) {
            throw exception(PRODUCT_NO_CHANGES);
        }
        submitTwoStageChange(userId, productId,
                ErpAuditStatus.EDITING.getStatus(),
                ErpAuditStatus.CONFIRM_PENDING.getStatus(),
                ErpAuditStatus.EDITING.getStatus(),
                ErpProductBpmConstants.SCENE_CODE_CHANGE_CONFIRM,
                "变更阶段二");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitObsoleteRequest(Long userId, Long productId, String reason) {
        if (StrUtil.isBlank(reason)) {
            throw exception(PRODUCT_OBSOLETE_REASON_REQUIRED);
        }
        submitTwoStageChange(userId, productId,
                ErpAuditStatus.APPROVE.getStatus(),
                ErpAuditStatus.OBSOLETE_CR_PENDING.getStatus(),
                ErpAuditStatus.APPROVE.getStatus(),
                ErpProductBpmConstants.SCENE_CODE_OBSOLETE_REQUEST,
                "废除审批");
        // 废除原因是最终留痕，CAS 成功后预写主表（abolish_reason/abolish_by）；
        // 置于 CAS 之后：守卫拒绝时不污染主表留痕字段
        productMapper.updateById(new ErpProductDO()
                .setId(productId)
                .setAbolishReason(reason)
                .setAbolishBy(userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitStatusChange(Long userId, Long productId, Integer targetStatus, String reason) {
        // 目标 status 写入暂存快照（审批通过才落主表，审批期间主表状态不变）
        ErpProductDO product = getRequiredProduct(productId);
        Integer currentStatus = product.getAuditStatus() != null ? product.getAuditStatus() : ErpAuditStatus.DRAFT.getStatus();
        if (ErpAuditStatus.isInApprovalFlow(currentStatus)) {
            throw exception(PRODUCT_BPM_SUBMIT_FAIL, "物料处于" + ErpAuditStatus.nameOf(currentStatus) + "，禁止重复提交");
        }
        if (!ErpAuditStatus.APPROVE.getStatus().equals(currentStatus)) {
            throw exception(PRODUCT_AUDIT_STATUS_ILLEGAL);
        }
        // 暂存仅携带目标 status（changeData 走 NOT_NULL 落库语义，其余字段不会被覆盖）
        ProductSaveReqVO statusReqVO = new ProductSaveReqVO();
        statusReqVO.setId(productId);
        statusReqVO.setStatus(targetStatus);
        var changes = pendingChangeService.diffProduct(product, statusReqVO);
        if (changes.isEmpty()) {
            throw exception(PRODUCT_NO_CHANGES);
        }
        submitTwoStageChange(userId, productId,
                ErpAuditStatus.APPROVE.getStatus(),
                ErpAuditStatus.STOP_PENDING.getStatus(),
                ErpAuditStatus.APPROVE.getStatus(),
                ErpProductBpmConstants.SCENE_CODE_STATUS_CHANGE,
                "启停审批");
        // CAS 成功后写暂存：diff 基于提交时主表值计算，守卫拒绝时不留暂存
        pendingChangeService.upsertPendingChange(productId, statusReqVO, changes.keySet(), reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTwoStageApproval(Long userId, Long productId, String reason) {
        ErpProductDO product = getRequiredProduct(productId);
        Integer currentStatus = product.getAuditStatus();
        String sceneCode;
        if (ErpAuditStatus.CR_PENDING.getStatus().equals(currentStatus)) {
            sceneCode = ErpProductBpmConstants.SCENE_CODE_CHANGE_REQUEST;
        } else if (ErpAuditStatus.CONFIRM_PENDING.getStatus().equals(currentStatus)) {
            sceneCode = ErpProductBpmConstants.SCENE_CODE_CHANGE_CONFIRM;
        } else if (ErpAuditStatus.OBSOLETE_CR_PENDING.getStatus().equals(currentStatus)) {
            sceneCode = ErpProductBpmConstants.SCENE_CODE_OBSOLETE_REQUEST;
        } else if (ErpAuditStatus.STOP_PENDING.getStatus().equals(currentStatus)) {
            sceneCode = ErpProductBpmConstants.SCENE_CODE_STATUS_CHANGE;
        } else {
            throw exception(PRODUCT_BPM_CANCEL_FAIL);
        }
        if (StrUtil.isBlank(product.getProcessInstanceId())) {
            throw exception(PRODUCT_BPM_CANCEL_FAIL);
        }
        String safeReason = StrUtil.isNotBlank(reason) ? reason : "发起人撤回";
        final String finalSceneCode = sceneCode;
        ErpTransactionUtils.afterCommit(() -> {
            try {
                approvalRuntimeService.cancel(finalSceneCode, productId, userId, safeReason);
            } catch (Exception e) {
                log.warn("[cancelTwoStageApproval] 撤回失败，productId={}, sceneCode={}", productId, finalSceneCode, e);
                throw e;
            }
        });
    }

    private ErpProductDO getRequiredProduct(Long productId) {
        ErpProductDO product = productMapper.selectById(productId);
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        return product;
    }

}
