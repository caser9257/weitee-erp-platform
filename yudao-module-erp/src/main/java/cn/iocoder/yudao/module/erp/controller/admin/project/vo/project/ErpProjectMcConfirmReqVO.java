package cn.iocoder.yudao.module.erp.controller.admin.project.vo.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 项目 MC 确认 Request VO")
@Data
public class ErpProjectMcConfirmReqVO {

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "项目编号不能为空")
    private Long projectId;

    @Schema(description = "MC 备注", requiredMode = Schema.RequiredMode.REQUIRED, example = "已确认物料齐套与备料方案")
    @NotBlank(message = "MC备注不能为空")
    private String remark;

}
