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

import javax.annotation.Resource;
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
    private BpmMessageService messageService;

    private Map<String, ApprovalResultHandler> resultHandlerMap;

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
        // 1. 更新快照状态
        approvalInstanceSnapshotService.updateSnapshotStatus(snapshot.getId(),
                BpmApprovalInstanceSnapshotStatusEnum.APPROVE.getStatus(), reason);

        // 2. 调用结果处理器
        try {
            handler.onApprove(bizId, processInstanceId, reason);
        } catch (Exception e) {
            log.error("[handleApprove][场景({}) 业务({}) 结果处理器异常]", snapshot.getSceneCode(), bizId, e);
        }

        // 3. 发送站内信通知
        try {
            messageService.sendMessageWhenProcessInstanceApprove(
                    new BpmMessageSendWhenProcessInstanceApproveReqDTO()
                            .setProcessInstanceId(processInstanceId)
                            .setProcessInstanceName(snapshot.getSceneCode())
                            .setStartUserId(Long.parseLong(snapshot.getCreator())));
        } catch (Exception e) {
            log.error("[handleApprove][场景({}) 业务({}) 发送通知异常]", snapshot.getSceneCode(), bizId, e);
        }
    }

    private void handleReject(BpmApprovalInstanceSnapshotDO snapshot, ApprovalResultHandler handler,
                              Long bizId, String processInstanceId, String reason) {
        // 1. 更新快照状态
        approvalInstanceSnapshotService.updateSnapshotStatus(snapshot.getId(),
                BpmApprovalInstanceSnapshotStatusEnum.REJECT.getStatus(), reason);

        // 2. 调用结果处理器
        try {
            handler.onReject(bizId, processInstanceId, reason);
        } catch (Exception e) {
            log.error("[handleReject][场景({}) 业务({}) 结果处理器异常]", snapshot.getSceneCode(), bizId, e);
        }

        // 3. 发送站内信通知
        try {
            messageService.sendMessageWhenProcessInstanceReject(
                    new BpmMessageSendWhenProcessInstanceRejectReqDTO()
                            .setProcessInstanceId(processInstanceId)
                            .setProcessInstanceName(snapshot.getSceneCode())
                            .setStartUserId(Long.parseLong(snapshot.getCreator()))
                            .setReason(reason));
        } catch (Exception e) {
            log.error("[handleReject][场景({}) 业务({}) 发送通知异常]", snapshot.getSceneCode(), bizId, e);
        }
    }

    private void handleCancel(BpmApprovalInstanceSnapshotDO snapshot, ApprovalResultHandler handler,
                              Long bizId, String processInstanceId, String reason) {
        // 快照状态已经在 BpmApprovalRuntimeServiceImpl.cancel 中更新，这里不需要再更新

        // 调用结果处理器
        try {
            handler.onCancel(bizId, processInstanceId, reason);
        } catch (Exception e) {
            log.error("[handleCancel][场景({}) 业务({}) 结果处理器异常]", snapshot.getSceneCode(), bizId, e);
        }
    }

}
