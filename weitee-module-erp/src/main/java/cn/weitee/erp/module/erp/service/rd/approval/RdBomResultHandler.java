package cn.weitee.erp.module.erp.service.rd.approval;

import cn.weitee.erp.module.bpm.service.approval.handler.ApprovalResultHandler;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.rd.ErpRdBomChangeType;
import cn.weitee.erp.module.erp.service.rd.ErpRdBomChangeLogService;
import cn.weitee.erp.module.erp.service.rd.ErpRdBomService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_NOT_EXISTS;

@Component
public class RdBomResultHandler implements ApprovalResultHandler {

    @Resource
    private ErpRdBomService rdBomService;
    @Resource
    private ErpRdBomChangeLogService changeLogService;

    @Override
    public String getSceneCode() {
        return cn.weitee.erp.module.erp.enums.ErpRdBomBpmConstants.SCENE_CODE;
    }

    @Override
    public void onApprove(Long bizId, String processInstanceId, String reason) {
        validateExists(bizId);
        rdBomService.updateRdBomStatusByBpm(bizId, processInstanceId, ErpAuditStatus.APPROVE.getStatus(), reason);
        changeLogService.logChange(bizId, ErpRdBomChangeType.APPROVE.getType(), "审批通过：" + reason);
    }

    @Override
    public void onReject(Long bizId, String processInstanceId, String reason) {
        validateExists(bizId);
        rdBomService.updateRdBomStatusByBpm(bizId, processInstanceId, ErpAuditStatus.REJECT.getStatus(), reason);
        changeLogService.logChange(bizId, ErpRdBomChangeType.REJECT.getType(), "审批驳回：" + reason);
    }

    @Override
    public void onCancel(Long bizId, String processInstanceId, String reason) {
        validateExists(bizId);
        rdBomService.rollbackRdBomStatusToDraftByBpm(bizId, processInstanceId, reason);
        changeLogService.logChange(bizId, ErpRdBomChangeType.CANCEL.getType(), "撤回审批：" + reason);
    }

    private void validateExists(Long bomId) {
        if (rdBomService.getRdBom(bomId) == null) {
            throw exception(RD_BOM_NOT_EXISTS);
        }
    }

}
