package cn.weitee.erp.module.erp.service.product.approval;

import cn.weitee.erp.module.bpm.service.approval.handler.ApprovalResultHandler;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 物料启停审批结果处理器（一段式：提交启停审批 → 审批通过即切换状态）
 *
 * 审批流守卫（终态单一路径）：
 * - onApprove：STOP_PENDING → APPROVE（目标 status 从暂存落主表，先业务后终态）
 * - onReject：STOP_PENDING → APPROVE（驳回，状态不变）
 * - onCancel：STOP_PENDING → APPROVE（撤回，状态不变）
 */
@Component
public class ProductStatusChangeResultHandler implements ApprovalResultHandler {

    @Resource
    private ErpProductService productService;

    @Override
    public String getSceneCode() {
        return cn.weitee.erp.module.erp.enums.ErpProductBpmConstants.SCENE_CODE_STATUS_CHANGE;
    }

    @Override
    public void onApprove(Long bizId, String processInstanceId, String reason) {
        productService.completeStatusChange(bizId, processInstanceId, true, reason);
    }

    @Override
    public void onReject(Long bizId, String processInstanceId, String reason) {
        productService.completeStatusChange(bizId, processInstanceId, false, reason);
    }

    @Override
    public void onCancel(Long bizId, String processInstanceId, String reason) {
        productService.completeStatusChange(bizId, processInstanceId, false, reason);
    }

}
