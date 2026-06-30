package cn.weitee.erp.module.erp.service.purchase.approval;

import cn.weitee.erp.module.bpm.service.approval.handler.ApprovalResultHandler;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseReturnMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseReturnService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_RETURN_NOT_EXISTS;

@Component
public class PurchaseReturnResultHandler implements ApprovalResultHandler {

    @Resource
    private ErpPurchaseReturnMapper purchaseReturnMapper;
    @Resource
    private ErpPurchaseReturnService purchaseReturnService;

    @Override
    public String getSceneCode() {
        return "erp.purchase.return.submit";
    }

    @Override
    public void onApprove(Long bizId, String processInstanceId, String reason) {
        validateExists(bizId);
        purchaseReturnService.updatePurchaseReturnStatusByBpm(bizId, processInstanceId,
                ErpAuditStatus.APPROVE.getStatus(), reason);
    }

    @Override
    public void onReject(Long bizId, String processInstanceId, String reason) {
        validateExists(bizId);
        purchaseReturnService.updatePurchaseReturnStatusByBpm(bizId, processInstanceId,
                ErpAuditStatus.REJECT.getStatus(), reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onCancel(Long bizId, String processInstanceId, String reason) {
        // 撤回/驳回：状态回 DRAFT，清理 processInstanceId，无库存/财务副作用
        purchaseReturnMapper.updateById(new ErpPurchaseReturnDO()
                .setId(bizId).setStatus(ErpAuditStatus.DRAFT.getStatus()));
        purchaseReturnMapper.clearProcessInstanceId(bizId);
    }

    private void validateExists(Long purchaseReturnId) {
        if (purchaseReturnMapper.selectById(purchaseReturnId) == null) {
            throw exception(PURCHASE_RETURN_NOT_EXISTS);
        }
    }
}
