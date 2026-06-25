package cn.weitee.erp.module.erp.controller.admin.project.vo.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 项目角色任务 Response VO")
@Data
public class ErpProjectRoleTaskRespVO {

    @Schema(description = "任务编号", example = "1")
    private Long id;

    @Schema(description = "项目编号", example = "1")
    private Long projectId;

    @Schema(description = "角色编码", example = "PC")
    private String roleCode;

    @Schema(description = "任务类型", example = "SALE_APPROVED_PLAN_CONFIRM")
    private String taskType;

    @Schema(description = "任务状态", example = "TODO")
    private String taskStatus;

    @Schema(description = "处理人用户编号", example = "100")
    private Long assigneeUserId;

    @Schema(description = "来源类型", example = "SALE_ORDER")
    private String sourceType;

    @Schema(description = "来源编号", example = "200")
    private Long sourceId;

    @Schema(description = "任务摘要", example = "销售订单审批已通过，请 PC 确认计划交付节点")
    private String summary;

    @Schema(description = "截止时间")
    private LocalDateTime dueTime;

    @Schema(description = "完成时间")
    private LocalDateTime finishTime;

    @Schema(description = "备注", example = "已确认")
    private String remark;

}
