package cn.iocoder.yudao.module.bpm.service.approval;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalInstanceSnapshotDO;
import cn.iocoder.yudao.module.bpm.enums.approval.BpmApprovalInstanceSnapshotStatusEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.bpm.service.approval.handler.ApprovalResultHandler;
import cn.iocoder.yudao.module.bpm.service.message.BpmMessageService;
import cn.iocoder.yudao.module.bpm.service.message.dto.BpmMessageSendWhenProcessInstanceApproveReqDTO;
import cn.iocoder.yudao.module.bpm.service.message.dto.BpmMessageSendWhenProcessInstanceRejectReqDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 审批事件统一分发器
 *
 * 替换现有各模块独立的 BpmProcessInstanceStatusEventListener 模式
 */
@Component
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class BpmApprovalEventDispatcher implements ApplicationListener<BpmProcessInstanceStatusEvent> {

    @Resource
    private BpmApprovalInstanceSnapshotService approvalInstanceSnapshotService;

    @Resource
    private BpmMessageService messageService;

    private Map<String, ApprovalResultHandler> resultHandlerMap;

    /**
     * 注入所有 ApprovalResultHandler，启动时校验 sceneCode 唯一性
     */
    @Resource
    public void setResultHandlers(List<ApprovalResultHandler> handlers) {
        this.resultHandlerMap = CollectionUtils.convertMap(handlers, ApprovalResultHandler::getSceneCode);
        Set<String> codes = handlers.stream().map(ApprovalResultHandler::getSceneCode).collect(Collectors.toSet());
        if (codes.size() != handlers.size()) {
            throw new IllegalStateException("[BpmApprovalEventDispatcher] 存在重复的 sceneCode，请检查配置");
        }
    }

    @Override
    public void onApplicationEvent(BpmProcessInstanceStatusEvent event) {
        // 1. 查找快照
        BpmApprovalInstanceSnapshotDO snapshot = approvalInstanceSnapshotService
                .getSnapshotByProcessInstanceId(event.getId());
        if (snapshot == null) {
            // 没有快照，说明不是通过平台发起的，忽略
            return;
        }

        // 1.1 幂等保护：重复事件且状态已经落库，直接忽略
        if (ObjectUtil.equal(snapshot.getStatus(), translateStatus(event.getStatus()))) {
            return;
        }

        // 2. 获取结果处理器
        ApprovalResultHandler handler = resultHandlerMap.get(snapshot.getSceneCode());
        if (handler == null) {
            log.warn("[onApplicationEvent][场景({}) 找不到结果处理器]", snapshot.getSceneCode());
            return;
        }

        // 3. 根据状态分发
        Integer status = event.getStatus();
        Long bizId = Long.parseLong(snapshot.getBizId());
        String processInstanceId = event.getId();
        String reason = event.getReason();

        if (ObjectUtil.equal(status, BpmProcessInstanceStatusEnum.APPROVE.getStatus())) {
            handleApprove(snapshot, handler, bizId, processInstanceId, reason);
        } else if (ObjectUtil.equal(status, BpmProcessInstanceStatusEnum.REJECT.getStatus())) {
            handleReject(snapshot, handler, bizId, processInstanceId, reason);
        } else if (ObjectUtil.equal(status, BpmProcessInstanceStatusEnum.CANCEL.getStatus())) {
            handleCancel(snapshot, handler, bizId, processInstanceId, reason);
        }
    }

    private Integer translateStatus(Integer eventStatus) {
        if (ObjectUtil.equal(eventStatus, BpmProcessInstanceStatusEnum.APPROVE.getStatus())) {
            return BpmApprovalInstanceSnapshotStatusEnum.APPROVE.getStatus();
        }
        if (ObjectUtil.equal(eventStatus, BpmProcessInstanceStatusEnum.REJECT.getStatus())) {
            return BpmApprovalInstanceSnapshotStatusEnum.REJECT.getStatus();
        }
        if (ObjectUtil.equal(eventStatus, BpmProcessInstanceStatusEnum.CANCEL.getStatus())) {
            return BpmApprovalInstanceSnapshotStatusEnum.CANCEL.getStatus();
        }
        return null;
    }

    private void handleApprove(BpmApprovalInstanceSnapshotDO snapshot, ApprovalResultHandler handler,
                               Long bizId, String processInstanceId, String reason) {
        // 1. 先调业务 handler — 失败时 snapshot 保持 PROCESSING，事件可重试
        handler.onApprove(bizId, processInstanceId, reason);

        // 2. 业务成功后再更新快照终态
        approvalInstanceSnapshotService.updateSnapshotStatus(snapshot.getId(),
                BpmApprovalInstanceSnapshotStatusEnum.APPROVE.getStatus(), reason);

        // 3. 通知移到事务提交后发送，避免通知阻塞主事务
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendApproveNotification(snapshot, processInstanceId, bizId);
                }
            });
        } else {
            // 无事务上下文时（如单元测试），直接发送
            sendApproveNotification(snapshot, processInstanceId, bizId);
        }
    }

    private void sendApproveNotification(BpmApprovalInstanceSnapshotDO snapshot,
                                         String processInstanceId, Long bizId) {
        try {
            if (isNotificationEnabled(snapshot.getNotifyJson(), "approve")) {
                messageService.sendMessageWhenProcessInstanceApprove(
                        new BpmMessageSendWhenProcessInstanceApproveReqDTO()
                                .setProcessInstanceId(processInstanceId)
                                .setProcessInstanceName(snapshot.getSceneCode())
                                .setStartUserId(Long.parseLong(snapshot.getCreator())));
            }
        } catch (Exception e) {
            log.error("[handleApprove][场景({}) 业务({}) 通知发送失败，不影响主流程]",
                    snapshot.getSceneCode(), bizId, e);
        }
    }

    private void handleReject(BpmApprovalInstanceSnapshotDO snapshot, ApprovalResultHandler handler,
                              Long bizId, String processInstanceId, String reason) {
        // 1. 先调业务 handler — 失败时 snapshot 保持 PROCESSING，事件可重试
        handler.onReject(bizId, processInstanceId, reason);

        // 2. 业务成功后再更新快照终态
        approvalInstanceSnapshotService.updateSnapshotStatus(snapshot.getId(),
                BpmApprovalInstanceSnapshotStatusEnum.REJECT.getStatus(), reason);

        // 3. 通知移到事务提交后发送，避免通知阻塞主事务
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendRejectNotification(snapshot, processInstanceId, bizId, reason);
                }
            });
        } else {
            // 无事务上下文时（如单元测试），直接发送
            sendRejectNotification(snapshot, processInstanceId, bizId, reason);
        }
    }

    private void sendRejectNotification(BpmApprovalInstanceSnapshotDO snapshot,
                                        String processInstanceId, Long bizId, String reason) {
        try {
            if (isNotificationEnabled(snapshot.getNotifyJson(), "reject")) {
                messageService.sendMessageWhenProcessInstanceReject(
                        new BpmMessageSendWhenProcessInstanceRejectReqDTO()
                                .setProcessInstanceId(processInstanceId)
                                .setProcessInstanceName(snapshot.getSceneCode())
                                .setStartUserId(Long.parseLong(snapshot.getCreator()))
                                .setReason(reason));
            }
        } catch (Exception e) {
            log.error("[handleReject][场景({}) 业务({}) 通知发送失败，不影响主流程]",
                    snapshot.getSceneCode(), bizId, e);
        }
    }

    private void handleCancel(BpmApprovalInstanceSnapshotDO snapshot, ApprovalResultHandler handler,
                              Long bizId, String processInstanceId, String reason) {
        // 幂等更新快照状态（仅 PROCESSING → CANCEL），不依赖调用方是否已更新
        approvalInstanceSnapshotService.updateSnapshotStatusIfProcessing(
                snapshot.getId(), BpmApprovalInstanceSnapshotStatusEnum.CANCEL.getStatus(), reason);

        // 调用结果处理器（非关键副作用，降级处理）
        try {
            handler.onCancel(bizId, processInstanceId, reason);
        } catch (Exception e) {
            log.error("[handleCancel][场景({}) 业务({}) 结果处理器异常]",
                    snapshot.getSceneCode(), bizId, e);
        }
    }

    /**
     * 判断通知是否启用
     *
     * @param notifyJson 通知配置 JSON
     * @param type       通知类型：taskCreated / approve / reject
     * @return 是否启用
     */
    @SuppressWarnings("unchecked")
    private boolean isNotificationEnabled(Map<String, Object> notifyJson, String type) {
        // 未配置时默认启用（兼容旧数据）
        if (notifyJson == null || notifyJson.isEmpty()) {
            return true;
        }
        try {
            Map<String, Object> typeConfig = (Map<String, Object>) notifyJson.get(type);
            if (typeConfig == null) {
                return true; // 未配置该类型时默认启用
            }
            Object enabled = typeConfig.get("enabled");
            if (enabled == null) {
                return true; // 未配置 enabled 时默认启用
            }
            return Boolean.TRUE.equals(enabled);
        } catch (Exception e) {
            log.warn("[isNotificationEnabled] 解析通知配置异常，默认启用", e);
            return true;
        }
    }

}
