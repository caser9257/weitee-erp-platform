package cn.weitee.erp.module.erp.service.product.approval;

import cn.weitee.erp.module.bpm.service.approval.handler.ApprovalResultHandler;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 物料两段式废除阶段一结果处理器（废除申请 → 审批 → 解锁"废除待执行"）
 *
 * 审批流守卫（终态单一路径）：
 * - onApprove：OBSOLETE_CR_PENDING → OBSOLETE_EDITING（废除待执行态）。
 *   两段式语义：段一审批授权"能不能废"，人工环节为反悔窗口（可查看影响面、可撤回），
 *   发起人确认后手动「提交废除审批」触发段二；不做自动流转，保证两段之间有人工环节。
 * - onReject：OBSOLETE_CR_PENDING → APPROVE（驳回，保持启用态）
 * - onCancel：OBSOLETE_CR_PENDING → APPROVE（撤回，保持启用态）
 */
@Component
public class ProductObsoleteRequestResultHandler implements ApprovalResultHandler {

    @Resource
    private ErpProductService productService;

    @Override
    public String getSceneCode() {
        return cn.weitee.erp.module.erp.enums.ErpProductBpmConstants.SCENE_CODE_OBSOLETE_REQUEST;
    }

    @Override
    public void onApprove(Long bizId, String processInstanceId, String reason) {
        productService.completeObsoleteRequest(bizId, processInstanceId, true, reason);
    }

    @Override
    public void onReject(Long bizId, String processInstanceId, String reason) {
        productService.completeObsoleteRequest(bizId, processInstanceId, false, reason);
    }

    @Override
    public void onCancel(Long bizId, String processInstanceId, String reason) {
        productService.completeObsoleteRequest(bizId, processInstanceId, false, reason);
    }

}
