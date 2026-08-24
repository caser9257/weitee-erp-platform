package cn.weitee.erp.module.erp.service.product.approval;

import cn.weitee.erp.module.bpm.service.approval.handler.ApprovalResultHandler;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_NOT_EXISTS;

@Component
public class ProductResultHandler implements ApprovalResultHandler {

    @Resource
    private ErpProductService productService;

    @Override
    public String getSceneCode() {
        return cn.weitee.erp.module.erp.enums.ErpProductBpmConstants.SCENE_CODE;
    }

    @Override
    public void onApprove(Long bizId, String processInstanceId, String reason) {
        validateExists(bizId);
        productService.updateProductAuditStatusByBpm(bizId, processInstanceId, ErpAuditStatus.APPROVE.getStatus(), reason);
    }

    @Override
    public void onReject(Long bizId, String processInstanceId, String reason) {
        validateExists(bizId);
        productService.updateProductAuditStatusByBpm(bizId, processInstanceId, ErpAuditStatus.REJECT.getStatus(), reason);
    }

    @Override
    public void onCancel(Long bizId, String processInstanceId, String reason) {
        validateExists(bizId);
        productService.rollbackProductAuditStatusToDraftByBpm(bizId, processInstanceId, reason);
    }

    private void validateExists(Long productId) {
        if (productService.getProduct(productId) == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
    }

}
