package cn.weitee.erp.module.mes.controller.admin.vo.taskexecution;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 现场执行任务查询请求。 */
@Data
public class MesTaskExecutionTaskReqVO {

    @NotBlank(message = "任务号不能为空")
    private String taskNo;
}
