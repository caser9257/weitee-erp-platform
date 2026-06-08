package cn.iocoder.yudao.module.project.controller.admin.vo.approve;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "管理后台 - 审批创建 Request VO")
@Data
public class ProjectApproveSaveReqVO {
    @Schema(description = "审批编号", example = "1024")
    private Long id;
    @Schema(description = "审批名称", required = true, example = "请假审批")
    @NotBlank(message = "审批名称不能为空")
    private String name;
}
