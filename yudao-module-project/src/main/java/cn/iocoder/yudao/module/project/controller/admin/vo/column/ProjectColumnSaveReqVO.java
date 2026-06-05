package cn.iocoder.yudao.module.project.controller.admin.vo.column;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 项目列表创建/更新 Request VO")
@Data
public class ProjectColumnSaveReqVO {
    @Schema(description = "列表编号")
    private Long id;
    @Schema(description = "项目编号", required = true)
    @NotNull(message = "项目编号不能为空")
    private Long projectId;
    @Schema(description = "列表名称", required = true, example = "待处理")
    @NotBlank(message = "列表名称不能为空")
    private String name;
    @Schema(description = "排序", example = "0")
    private Integer sort;
}
