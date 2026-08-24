package cn.weitee.erp.module.bpm.service.approval.generic;

import cn.weitee.erp.module.bpm.service.approval.handler.ApprovalResultHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 通用审批结果处理器（兜底）
 *
 * 为配置了 generic_config 的场景处理审批结果回写，无需业务模块编写 Handler。
 * 回写规则由 generic_config.statusMapping 声明：approve/reject/cancel → 业务状态值。
 */
@Component
@Slf4j
public class GenericApprovalResultHandler implements ApprovalResultHandler {

    @Resource
    private GenericBizService genericBizService;

    @Resource
    private GenericApprovalConfigService genericConfigService;

    /** 兜底标识：不会被任何业务场景精确命中（业务场景码为 erp.xxx.submit 格式） */
    public static final String GENERIC_SCENE_CODE = "bpm.generic.approval";

    /** 兜底标识：不会被精确场景命中 */
    @Override
    public String getSceneCode() {
        return GENERIC_SCENE_CODE;
    }

    @Override
    public void onApprove(Long bizId, String processInstanceId, String reason) {
        throw new UnsupportedOperationException("通用结果处理器需通过带场景的方法调用");
    }

    @Override
    public void onReject(Long bizId, String processInstanceId, String reason) {
        throw new UnsupportedOperationException("通用结果处理器需通过带场景的方法调用");
    }

    @Override
    public void onCancel(Long bizId, String processInstanceId, String reason) {
        throw new UnsupportedOperationException("通用结果处理器需通过带场景的方法调用");
    }

    /** 带场景的审批通过回写 */
    public void onApproveWithScene(String sceneCode, Long bizId, String processInstanceId, String snapshotId, String reason) {
        updateStatus(sceneCode, bizId, GenericApprovalConfig.KEY_APPROVE);
    }

    /** 带场景的审批驳回回写 */
    public void onRejectWithScene(String sceneCode, Long bizId, String processInstanceId, String snapshotId, String reason) {
        updateStatus(sceneCode, bizId, GenericApprovalConfig.KEY_REJECT);
    }

    /** 带场景的审批撤回回写 */
    public void onCancelWithScene(String sceneCode, Long bizId, String processInstanceId, String snapshotId, String reason) {
        updateStatus(sceneCode, bizId, GenericApprovalConfig.KEY_CANCEL);
    }

    private void updateStatus(String sceneCode, Long bizId, String statusKey) {
        GenericApprovalConfig config = genericConfigService.getConfig(sceneCode);
        if (config == null) {
            log.warn("[updateStatus] 场景({}) 未启用通用审批接入", sceneCode);
            return;
        }
        Integer status = config.getStatus(statusKey);
        if (status == null) {
            log.warn("[updateStatus] 场景({}) 未配置 statusMapping.{}，跳过回写", sceneCode, statusKey);
            return;
        }
        int count = genericBizService.updateStatus(config, bizId, status);
        log.info("[updateStatus] 通用审批结果回写，sceneCode={}, bizId={}, statusKey={}, status={}, count={}",
                sceneCode, bizId, statusKey, status, count);
    }

}
