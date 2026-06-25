package cn.iocoder.yudao.module.bpm.service.message;

import cn.iocoder.yudao.module.bpm.service.message.dto.BpmMessageSendWhenProcessInstanceApproveReqDTO;
import cn.iocoder.yudao.module.bpm.service.message.dto.BpmMessageSendWhenProcessInstanceCancelReqDTO;
import cn.iocoder.yudao.module.bpm.service.message.dto.BpmMessageSendWhenProcessInstanceRejectReqDTO;
import cn.iocoder.yudao.module.bpm.service.message.dto.BpmMessageSendWhenTaskCreatedReqDTO;
import cn.iocoder.yudao.module.bpm.service.message.dto.BpmMessageSendWhenTaskDelegateReqDTO;
import cn.iocoder.yudao.module.bpm.service.message.dto.BpmMessageSendWhenTaskTimeoutReqDTO;
import cn.iocoder.yudao.module.bpm.service.message.dto.BpmMessageSendWhenTaskTransferReqDTO;
import cn.iocoder.yudao.module.bpm.service.message.dto.BpmMessageSendWhenTaskUrgeReqDTO;

import jakarta.validation.Valid;

/**
 * BPM 消息 Service 接口
 *
 * @author WeTai
 */
public interface BpmMessageService {

    /**
     * 发送流程实例被通过的消息
     *
     * @param reqDTO 发送信息
     */
    void sendMessageWhenProcessInstanceApprove(@Valid BpmMessageSendWhenProcessInstanceApproveReqDTO reqDTO);

    /**
     * 发送流程实例被不通过的消息
     *
     * @param reqDTO 发送信息
     */
    void sendMessageWhenProcessInstanceReject(@Valid BpmMessageSendWhenProcessInstanceRejectReqDTO reqDTO);

    /**
     * 发送流程实例被撤回的消息
     *
     * @param reqDTO 发送信息
     */
    void sendMessageWhenProcessInstanceCancel(@Valid BpmMessageSendWhenProcessInstanceCancelReqDTO reqDTO);

    /**
     * 发送任务被分配的消息
     *
     * @param reqDTO 发送信息
     */
    void sendMessageWhenTaskAssigned(@Valid BpmMessageSendWhenTaskCreatedReqDTO reqDTO);

    /**
     * 发送任务审批超时的消息
     *
     * @param reqDTO 发送信息
     */
    void sendMessageWhenTaskTimeout(@Valid BpmMessageSendWhenTaskTimeoutReqDTO reqDTO);

    /**
     * 发送任务被催办的消息
     *
     * @param reqDTO 发送信息
     */
    void sendMessageWhenTaskUrge(@Valid BpmMessageSendWhenTaskUrgeReqDTO reqDTO);

    /**
     * 发送任务被转办的消息
     *
     * @param reqDTO 发送信息
     */
    void sendMessageWhenTaskTransfer(@Valid BpmMessageSendWhenTaskTransferReqDTO reqDTO);

    /**
     * 发送任务被委托的消息
     *
     * @param reqDTO 发送信息
     */
    void sendMessageWhenTaskDelegate(@Valid BpmMessageSendWhenTaskDelegateReqDTO reqDTO);

}
