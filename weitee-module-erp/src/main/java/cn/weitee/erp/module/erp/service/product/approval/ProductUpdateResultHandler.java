package cn.weitee.erp.module.erp.service.product.approval;

import cn.weitee.erp.module.bpm.service.approval.handler.ApprovalResultHandler;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductPendingChangeDO;
import cn.weitee.erp.module.erp.enums.ErpProductBpmConstants;
import cn.weitee.erp.module.erp.service.product.ErpProductPendingChangeService;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 物料修改审批结果处理器
 *
 * 三条路径统一收口（终态单一路径）：
 * - 通过：暂存快照落主表 → 主表恢复 APPROVE
 * - 驳回：暂存留痕 REJECTED → 主表恢复 APPROVE（主表从未变更，无回滚动作）
 * - 撤回：删除暂存 → 主表恢复 APPROVE
 */
@Component
@Slf4j
public class ProductUpdateResultHandler implements ApprovalResultHandler {

    @Resource
    private ErpProductPendingChangeService pendingChangeService;
    @Resource
    private ErpProductService productService;

    @Override
    public String getSceneCode() {
        return ErpProductBpmConstants.SCENE_CODE_UPDATE;
    }

    @Override
    public void onApprove(Long bizId, String processInstanceId, String reason) {
        // 先业务后终态：落库成功后才恢复主表状态；重复事件由 apply 幂等跳过
        ErpProductPendingChangeDO pending = pendingChangeService.getPendingChange(bizId);
        if (pending == null) {
            log.warn("[onApprove] 暂存记录不存在，跳过，productId={}", bizId);
            return;
        }
        // Finding 1 前置校验：只处理 PENDING 状态的暂存，FAILED 批次即使收到回调也拒绝应用
        if (!ErpProductPendingChangeDO.STATUS_PENDING.equals(pending.getStatus())) {
            log.warn("[onApprove] 暂存非 PENDING 状态（{}），拒绝应用，productId={}, pi={}",
                    pending.getStatus(), bizId, processInstanceId);
            return;
        }
        if (!isCurrentProcess(pending, processInstanceId)) {
            log.warn("[onApprove] 流程实例不匹配，拒绝应用，productId={}, callbackPi={}, pendingPi={}",
                    bizId, processInstanceId, pending.getProcessInstanceId());
            return;
        }
        boolean applied = pendingChangeService.applyPendingChange(bizId, processInstanceId, reason);
        if (applied) {
            productService.restoreProductAfterUpdateApproval(bizId, processInstanceId);
            log.info("[onApprove] 物料修改审批通过并已生效，productId={}, pi={}", bizId, processInstanceId);
        }
    }

    @Override
    public void onReject(Long bizId, String processInstanceId, String reason) {
        ErpProductPendingChangeDO pending = pendingChangeService.getPendingChange(bizId);
        if (pending == null || !isCurrentProcess(pending, processInstanceId)) {
            log.warn("[onReject] 暂存不存在或流程实例不匹配，拒绝处理，productId={}, callbackPi={}, pendingPi={}",
                    bizId, processInstanceId, pending == null ? null : pending.getProcessInstanceId());
            return;
        }
        boolean rejected = pendingChangeService.rejectPendingChange(bizId, processInstanceId, reason);
        if (rejected) {
            productService.restoreProductAfterUpdateApproval(bizId, processInstanceId);
            log.info("[onReject] 物料修改审批驳回，变更已丢弃，productId={}, pi={}", bizId, processInstanceId);
        }
    }

    @Override
    public void onCancel(Long bizId, String processInstanceId, String reason) {
        ErpProductPendingChangeDO pending = pendingChangeService.getPendingChange(bizId);
        if (pending == null || !isCurrentProcess(pending, processInstanceId)) {
            log.warn("[onCancel] 暂存不存在或流程实例不匹配，拒绝处理，productId={}, callbackPi={}, pendingPi={}",
                    bizId, processInstanceId, pending == null ? null : pending.getProcessInstanceId());
            return;
        }
        pendingChangeService.discardPendingChange(bizId);
        productService.restoreProductAfterCancel(bizId);
        log.info("[onCancel] 物料修改审批撤回，productId={}, pi={}", bizId, processInstanceId);
    }

    private boolean isCurrentProcess(ErpProductPendingChangeDO pending, String processInstanceId) {
        return ErpProductPendingChangeDO.STATUS_PENDING.equals(pending.getStatus())
                && processInstanceId != null
                && processInstanceId.equals(pending.getProcessInstanceId());
    }

}
