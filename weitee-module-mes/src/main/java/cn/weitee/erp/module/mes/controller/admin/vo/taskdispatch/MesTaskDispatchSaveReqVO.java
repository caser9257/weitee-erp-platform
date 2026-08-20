package cn.weitee.erp.module.mes.controller.admin.vo.taskdispatch;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 派工请求。 */
@Data
public class MesTaskDispatchSaveReqVO {

    @NotNull(message = "工序任务不能为空")
    private Long taskId;

    private Long deviceId;

    private Long teamId;

    private Long workerUserId;

    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
