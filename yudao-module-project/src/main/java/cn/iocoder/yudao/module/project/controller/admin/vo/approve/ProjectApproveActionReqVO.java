package cn.iocoder.yudao.module.project.controller.admin.vo.approve;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "管理后台 - 审批操作 Request VO")
@Data
public class ProjectApproveActionReqVO {
    @Schema(description = "审批编号", required = true, example = "1024")
    @NotBlank(message = "审批编号不能为空")
    private Long id;
    @Schema(description = "操作", required = true, example = "agree")
    @NotBlank(message = "操作不能为空")
    private String action;
    @Schema(description = "审批意见", example = "同意")
    private String content;
}
