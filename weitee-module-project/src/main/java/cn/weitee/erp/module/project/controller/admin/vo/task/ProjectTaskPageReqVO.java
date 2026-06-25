package cn.weitee.erp.module.project.controller.admin.vo.task;

import cn.weitee.erp.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 任务分页列表 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectTaskPageReqVO extends PageParam {
    @Schema(description = "项目编号")
    private Long projectId;
    @Schema(description = "列表编号")
    private Long columnId;
    @Schema(description = "父任务ID")
    private Long parentId;
    @Schema(description = "任务标题")
    private String name;
    @Schema(description = "是否已完成")
    private Boolean completed;
    @Schema(description = "是否已归档")
    private Boolean archived;
    @Schema(description = "负责人用户ID")
    private Long ownerUserId;
    @Schema(description = "工作流状态ID")
    private Long flowItemId;
}
