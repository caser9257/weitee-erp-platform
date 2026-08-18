package cn.weitee.erp.module.bpm.service.approval;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.common.util.collection.CollectionUtils;
import cn.weitee.erp.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalInstanceSnapshotDO;
import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalRecordDO;
import cn.weitee.erp.module.bpm.enums.approval.BpmApprovalInstanceSnapshotStatusEnum;
import cn.weitee.erp.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.weitee.erp.module.bpm.service.approval.generic.GenericApprovalConfigService;
import cn.weitee.erp.module.bpm.service.approval.generic.GenericApprovalResultHandler;
import cn.weitee.erp.module.bpm.service.approval.handler.ApprovalResultHandler;
import cn.weitee.erp.module.bpm.service.approval.handler.SnapshotAwareApprovalResultHandler;
import cn.weitee.erp.module.bpm.service.message.BpmMessageService;
import cn.weitee.erp.module.bpm.service.message.dto.BpmMessageSendWhenProcessInstanceApproveReqDTO;
import cn.weitee.erp.module.bpm.service.message.dto.BpmMessageSendWhenProcessInstanceRejectReqDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 审批事件统一分发器
 *
 * 替换现有各模块独立的 BpmProcessInstanceStatusEventListener 模式
 */
@Component
@Slf4j
public class BpmApprovalEventDispatcher implements ApplicationListener<BpmProcessInstanceStatusEvent> {

    @Resource
    private BpmApprovalInstanceSnapshotService approvalInstanceSnapshotService;

    @Resource
    private BpmApprovalRecordService approvalRecordService;

    @Resource
    private BpmMessageService messageService;

    private Map<String, ApprovalResultHandler> resultHandlerMap;

    @Resource
    private GenericApprovalConfigService genericConfigService;

    @Resource
    private GenericApprovalResultHandler genericApprovalResultHandler;

    /**
     * 注入所有 ApprovalResultHandler
     */
    @Resource
    public void setResultHandlers(List<ApprovalResultHandler> handlers) {
        this.resultHandlerMap = CollectionUtils.convertMap(handlers, ApprovalResultHandler::getSceneCode);
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
        //     但 FAILED 状态不拦截 — 可能是之前 BPM 调用失败后重试的事件
        if (ObjectUtil.equal(snapshot.getStatus(), translateStatus(event.getStatus()))
                && !ObjectUtil.equal(snapshot.getStatus(), BpmApprovalInstanceSnapshotStatusEnum.FAILED.getStatus())) {
            return;
        }

        // 2. 获取结果处理器（精确匹配；未命中时兜底通用审批接入）
        ApprovalResultHandler handler = resultHandlerMap.get(snapshot.getSceneCode());
        if (handler == null && genericConfigService.isGenericEnabled(snapshot.getSceneCode())) {
            handler = genericApprovalResultHandler;
        }
        if (handler == null) {
            log.warn("[onApplicationEvent][场景({}) 找不到结果处理器]", snapshot.getSceneCode());
            return;
        }

        // 3. 根据状态分发
        Integer status = event.getStatus();
        Long bizId = Long.parseLong(snapshot.getBizId());
        String processInstanceId = event.getId();
        String snapshotId = String.valueOf(snapshot.getId());
        String reason = event.getReason();

        if (ObjectUtil.equal(status, BpmProcessInstanceStatusEnum.APPROVE.getStatus())) {
            handleApprove(snapshot, handler, bizId, processInstanceId, snapshotId, reason);
        } else if (ObjectUtil.equal(status, BpmProcessInstanceStatusEnum.REJECT.getStatus())) {
            handleReject(snapshot, handler, bizId, processInstanceId, snapshotId, reason);
        } else if (ObjectUtil.equal(status, BpmProcessInstanceStatusEnum.CANCEL.getStatus())) {
            handleCancel(snapshot, handler, bizId, processInstanceId, snapshotId, reason);
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
                               Long bizId, String processInstanceId, String snapshotId, String reason) {
        // 1. 先调用结果处理器（业务状态回写）
        //    如果处理器失败，快照保持"审批中"状态，避免快照与业务状态不一致
        try {
            if (handler instanceof GenericApprovalResultHandler genericHandler) {
                genericHandler.onApproveWithScene(snapshot.getSceneCode(), bizId, processInstanceId, snapshotId, reason);
            } else if (handler instanceof SnapshotAwareApprovalResultHandler snapshotAwareHandler) {
                snapshotAwareHandler.onApproveWithSnapshot(bizId, processInstanceId, snapshotId, reason);
            } else {
                handler.onApprove(bizId, processInstanceId, reason);
            }
        } catch (Exception e) {
            log.error("[handleApprove][场景({}) 业务({}) 结果处理器异常，快照状态未更新]", snapshot.getSceneCode(), bizId, e);
            return; // 处理器失败，不更新快照状态，保持"审批中"
        }

        // 2. 处理器成功后，更新快照状态为"通过"
        approvalInstanceSnapshotService.updateSnapshotStatus(snapshot.getId(),
                BpmApprovalInstanceSnapshotStatusEnum.APPROVE.getStatus(), reason);

        // 3. 记录审批操作
        try {
            BpmApprovalRecordDO record = BpmApprovalRecordDO.builder()
                    .approvalId(snapshot.getApprovalId())
                    .action("APPROVE")
                    .operatorUserId(snapshot.getStartUserId())
                    .comment(reason)
                    .build();
            approvalRecordService.createRecord(record);
        } catch (Exception e) {
            log.error("[handleApprove][场景({}) 业务({}) 记录审批操作异常]", snapshot.getSceneCode(), bizId, e);
        }

        // 4. 发送站内信通知
        try {
            messageService.sendMessageWhenProcessInstanceApprove(
                    new BpmMessageSendWhenProcessInstanceApproveReqDTO()
                            .setProcessInstanceId(processInstanceId)
                            .setProcessInstanceName(snapshot.getSceneCode())
                            .setStartUserId(snapshot.getStartUserId()));
        } catch (Exception e) {
            log.error("[handleApprove][场景({}) 业务({}) 发送通知异常]", snapshot.getSceneCode(), bizId, e);
        }
    }

    private void handleReject(BpmApprovalInstanceSnapshotDO snapshot, ApprovalResultHandler handler,
                              Long bizId, String processInstanceId, String snapshotId, String reason) {
        // 1. 先调用结果处理器（业务状态回写）
        //    如果处理器失败，快照保持"审批中"状态，避免快照与业务状态不一致
        try {
            if (handler instanceof GenericApprovalResultHandler genericHandler) {
                genericHandler.onRejectWithScene(snapshot.getSceneCode(), bizId, processInstanceId, snapshotId, reason);
            } else if (handler instanceof SnapshotAwareApprovalResultHandler snapshotAwareHandler) {
                snapshotAwareHandler.onRejectWithSnapshot(bizId, processInstanceId, snapshotId, reason);
            } else {
                handler.onReject(bizId, processInstanceId, reason);
            }
        } catch (Exception e) {
            log.error("[handleReject][场景({}) 业务({}) 结果处理器异常，快照状态未更新]", snapshot.getSceneCode(), bizId, e);
            return; // 处理器失败，不更新快照状态，保持"审批中"
        }

        // 2. 处理器成功后，更新快照状态为"驳回"
        approvalInstanceSnapshotService.updateSnapshotStatus(snapshot.getId(),
                BpmApprovalInstanceSnapshotStatusEnum.REJECT.getStatus(), reason);

        // 3. 记录审批操作
        try {
            BpmApprovalRecordDO record = BpmApprovalRecordDO.builder()
                    .approvalId(snapshot.getApprovalId())
                    .action("REJECT")
                    .operatorUserId(snapshot.getStartUserId())
                    .comment(reason)
                    .build();
            approvalRecordService.createRecord(record);
        } catch (Exception e) {
            log.error("[handleReject][场景({}) 业务({}) 记录审批操作异常]", snapshot.getSceneCode(), bizId, e);
        }

        // 4. 发送站内信通知
        try {
            messageService.sendMessageWhenProcessInstanceReject(
                    new BpmMessageSendWhenProcessInstanceRejectReqDTO()
                            .setProcessInstanceId(processInstanceId)
                            .setProcessInstanceName(snapshot.getSceneCode())
                            .setStartUserId(snapshot.getStartUserId())
                            .setReason(reason));
        } catch (Exception e) {
            log.error("[handleReject][场景({}) 业务({}) 发送通知异常]", snapshot.getSceneCode(), bizId, e);
        }
    }

    private void handleCancel(BpmApprovalInstanceSnapshotDO snapshot, ApprovalResultHandler handler,
                              Long bizId, String processInstanceId, String snapshotId, String reason) {
        // 1. 先调用结果处理器（业务状态回写）
        //    如果处理器失败，快照保持"审批中"状态，避免快照与业务状态不一致
        try {
            if (handler instanceof GenericApprovalResultHandler genericHandler) {
                genericHandler.onCancelWithScene(snapshot.getSceneCode(), bizId, processInstanceId, snapshotId, reason);
            } else if (handler instanceof SnapshotAwareApprovalResultHandler snapshotAwareHandler) {
                snapshotAwareHandler.onCancelWithSnapshot(bizId, processInstanceId, snapshotId, reason);
            } else {
                handler.onCancel(bizId, processInstanceId, reason);
            }
        } catch (Exception e) {
            log.error("[handleCancel][场景({}) 业务({}) 结果处理器异常，快照状态未更新]", snapshot.getSceneCode(), bizId, e);
            return; // 处理器失败，不更新快照状态，保持"审批中"
        }

        // 2. 处理器成功后，更新快照状态为"撤回"
        approvalInstanceSnapshotService.updateSnapshotStatus(snapshot.getId(),
                BpmApprovalInstanceSnapshotStatusEnum.CANCEL.getStatus(), reason);

        // 3. 记录审批操作
        try {
            BpmApprovalRecordDO record = BpmApprovalRecordDO.builder()
                    .approvalId(snapshot.getApprovalId())
                    .action("CANCEL")
                    .operatorUserId(snapshot.getStartUserId())
                    .comment(reason)
                    .build();
            approvalRecordService.createRecord(record);
        } catch (Exception e) {
            log.error("[handleCancel][场景({}) 业务({}) 记录审批操作异常]", snapshot.getSceneCode(), bizId, e);
        }
    }

}
