package cn.weitee.erp.module.erp.service.product.approval;

import cn.weitee.erp.module.bpm.service.approval.handler.ApprovalResultHandler;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 物料两段式变更阶段一结果处理器（变更申请 → 审批 → 解锁编辑权限）
 *
 * 审批流守卫（终态单一路径）：所有阶段流转由本类统一处理，禁止在 submit/cancel 入口另行写状态。
 * - onApprove：CR_PENDING → EDITING（解锁编辑权限）
 * - onReject：CR_PENDING → APPROVE（驳回，保持原数据不变）
 * - onCancel：CR_PENDING → APPROVE（撤回，保持原数据不变）
 */
@Component
public class ProductChangeRequestResultHandler implements ApprovalResultHandler {

    @Resource
    private ErpProductService productService;

    @Override
    public String getSceneCode() {
        return cn.weitee.erp.module.erp.enums.ErpProductBpmConstants.SCENE_CODE_CHANGE_REQUEST;
    }

    @Override
    public void onApprove(Long bizId, String processInstanceId, String reason) {
        productService.completeChangeRequest(bizId, processInstanceId, true, reason);
    }

    @Override
    public void onReject(Long bizId, String processInstanceId, String reason) {
        productService.completeChangeRequest(bizId, processInstanceId, false, reason);
    }

    @Override
    public void onCancel(Long bizId, String processInstanceId, String reason) {
        productService.completeChangeRequest(bizId, processInstanceId, false, reason);
    }

}
