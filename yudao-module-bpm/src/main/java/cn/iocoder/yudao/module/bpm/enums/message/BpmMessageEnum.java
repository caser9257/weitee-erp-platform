package cn.iocoder.yudao.module.bpm.enums.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Bpm 消息的枚举
 *
 * @author WeTai
 */
@AllArgsConstructor
@Getter
public enum BpmMessageEnum {

    PROCESS_INSTANCE_APPROVE("bpm_process_instance_approve"), // 流程任务被审批通过时，发送给申请人
    PROCESS_INSTANCE_REJECT("bpm_process_instance_reject"), // 流程任务被审批不通过时，发送给申请人
    PROCESS_INSTANCE_CANCEL("bpm_process_instance_cancel"), // 流程任务被撤回时，发送给申请人
    TASK_ASSIGNED("bpm_task_assigned"), // 任务被分配时，发送给审批人
    TASK_TIMEOUT("bpm_task_timeout"), // 任务审批超时时，发送给审批人
    TASK_URGE("bpm_task_urge"), // 任务被催办时，发送给审批人
    TASK_TRANSFER("bpm_task_transfer"), // 任务被转办时，发送给新审批人
    TASK_DELEGATE("bpm_task_delegate"); // 任务被委托时，发送给代理人

    /**
     * 短信模板的标识
     *
     * 关联 SmsTemplateDO 的 code 属性
     */
    private final String smsTemplateCode;

}
