package cn.weitee.erp.module.project.controller.admin.vo.approve;

import cn.weitee.erp.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 审批分页列表 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProjectApprovePageReqVO extends PageParam {
    @Schema(description = "审批名称", example = "请假")
    private String name;
    @Schema(description = "审批状态", example = "0")
    private Integer status;
}
