package cn.weitee.erp.module.erp.service.mrp.approval;

import cn.weitee.erp.module.bpm.service.approval.handler.ApprovalResultHandler;
import cn.weitee.erp.module.erp.enums.ErpBomBpmConstants;
import cn.weitee.erp.module.erp.enums.mrp.ErpBomStatusEnum;
import cn.weitee.erp.module.erp.service.mrp.ErpBomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 制造 BOM 停用申请结果处理器
 *
 * 审批通过才落终态（DISABLE）；驳回/撤回无需任何回滚（主表从未变更）。
 */
@Component
@Slf4j
public class BomDisableResultHandler implements ApprovalResultHandler {

    @Resource
    private ErpBomService bomService;

    @Override
    public String getSceneCode() {
        return ErpBomBpmConstants.SCENE_CODE_DISABLE;
    }

    @Override
    public void onApprove(Long bizId, String processInstanceId, String reason) {
        bomService.updateBomStatus(bizId, ErpBomStatusEnum.DISABLE.getStatus());
        bomService.clearDisableApprovalProcess(bizId);
        log.info("[onApprove] 制造 BOM 停用生效，bomId={}, pi={}", bizId, processInstanceId);
    }

    @Override
    public void onReject(Long bizId, String processInstanceId, String reason) {
        bomService.clearDisableApprovalProcess(bizId);
        log.info("[onReject] 制造 BOM 停用申请驳回，BOM 保持启用，bomId={}, pi={}", bizId, processInstanceId);
    }

    @Override
    public void onCancel(Long bizId, String processInstanceId, String reason) {
        bomService.clearDisableApprovalProcess(bizId);
        log.info("[onCancel] 制造 BOM 停用申请撤回，bomId={}, pi={}", bizId, processInstanceId);
    }

}
