package cn.weitee.erp.module.mes.controller.admin.vo.worktask;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MesWorkTaskUpdatePlanTimeReqVO {

    @NotNull(message = "任务不能为空")
    private Long id;

    @NotNull(message = "计划开始时间不能为空")
    private LocalDateTime planStartTime;

    @NotNull(message = "计划结束时间不能为空")
    private LocalDateTime planEndTime;

    private String remark;

}
