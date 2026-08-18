package cn.weitee.erp.module.bpm.service.approval.generic;

import cn.weitee.erp.module.bpm.service.approval.BpmApprovalRuntimeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.bpm.enums.ErrorCodeConstants.APPROVAL_SCENE_NOT_EXISTS;

/**
 * 通用审批提交服务实现
 *
 * 事务铁律：事务内写 SUBMIT 状态，afterCommit 启动 BPM，失败回写 FAILED（支持重试）。
 */
@Service
@Validated
@Slf4j
public class GenericApprovalSubmitServiceImpl implements GenericApprovalSubmitService {

    @Resource
    private GenericApprovalConfigService genericConfigService;

    @Resource
    private GenericBizService genericBizService;

    @Resource
    private BpmApprovalRuntimeService approvalRuntimeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submit(String sceneCode, Long bizId, Long userId) {
        GenericApprovalConfig config = genericConfigService.getConfig(sceneCode);
        if (config == null) {
            throw exception(APPROVAL_SCENE_NOT_EXISTS, sceneCode);
        }

        // 1. 事务内：业务状态 → SUBMIT，清空流程实例ID
        Integer submitStatus = config.getStatus(GenericApprovalConfig.KEY_SUBMIT);
        if (submitStatus == null) {
            throw new IllegalArgumentException("通用审批配置缺少 statusMapping.submit: " + sceneCode);
        }
        int count = genericBizService.updateStatus(config, bizId, submitStatus);
        if (count == 0) {
            throw new IllegalArgumentException("通用审批业务单不存在或状态更新失败，sceneCode=" + sceneCode + ", bizId=" + bizId);
        }
        genericBizService.updateProcessInstanceId(config, bizId, null);

        // 2. 事务外：启动 BPM，成功回写流程实例ID，失败回写 FAILED
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    String processInstanceId = approvalRuntimeService.submit(sceneCode, bizId, userId);
                    genericBizService.updateProcessInstanceId(config, bizId, processInstanceId);
                } catch (Exception e) {
                    log.error("[submit] 通用审批 BPM 创建失败，sceneCode={}, bizId={}", sceneCode, bizId, e);
                    try {
                        genericBizService.updateStatus(config, bizId, config.getStatus(GenericApprovalConfig.KEY_FAILED));
                        genericBizService.updateProcessInstanceId(config, bizId, null);
                    } catch (Exception ex) {
                        log.error("[submit] 通用审批回写 FAILED 状态失败，sceneCode={}, bizId={}", sceneCode, bizId, ex);
                    }
                }
            }
        });
        // 受理语义：流程实例异步创建，由列表刷新后校验
        return null;
    }

    @Override
    public void cancel(String sceneCode, Long bizId, Long userId, String reason) {
        // 铁律 3：撤回只触发 BPM 撤回，业务终态（回草稿）由事件分发器 → GenericResultHandler 统一回写
        approvalRuntimeService.cancel(sceneCode, bizId, userId, reason);
    }

}
