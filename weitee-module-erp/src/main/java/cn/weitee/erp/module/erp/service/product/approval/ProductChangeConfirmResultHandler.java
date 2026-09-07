package cn.weitee.erp.module.erp.service.product.approval;

import cn.weitee.erp.module.bpm.service.approval.handler.ApprovalResultHandler;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 物料两段式变更阶段二结果处理器（变更完成确认 → 审批 → 生效落库）
 *
 * 审批流守卫（终态单一路径）：
 * - onApprove：CONFIRM_PENDING → APPROVE（暂存快照落主表，变更生效）
 * - onReject：CONFIRM_PENDING → EDITING（驳回，保留已改内容可继续修）
 * - onCancel：CONFIRM_PENDING → EDITING（撤回，保留已改内容）
 */
@Component
public class ProductChangeConfirmResultHandler implements ApprovalResultHandler {

    @Resource
    private ErpProductService productService;

    @Override
    public String getSceneCode() {
        return cn.weitee.erp.module.erp.enums.ErpProductBpmConstants.SCENE_CODE_CHANGE_CONFIRM;
    }

    @Override
    public void onApprove(Long bizId, String processInstanceId, String reason) {
        productService.completeChangeConfirm(bizId, processInstanceId, true, reason);
    }

    @Override
    public void onReject(Long bizId, String processInstanceId, String reason) {
        productService.completeChangeConfirm(bizId, processInstanceId, false, reason);
    }

    @Override
    public void onCancel(Long bizId, String processInstanceId, String reason) {
        productService.completeChangeConfirm(bizId, processInstanceId, false, reason);
    }

}