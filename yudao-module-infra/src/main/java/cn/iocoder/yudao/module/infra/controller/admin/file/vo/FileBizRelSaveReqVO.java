package cn.iocoder.yudao.module.infra.controller.admin.file.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 文件业务关联创建/更新 Request VO")
@Data
public class FileBizRelSaveReqVO {

    @Schema(description = "文件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "文件ID不能为空")
    private Long fileId;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "业务类型不能为空")
    private String bizType;

    @Schema(description = "业务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "业务ID不能为空")
    private Long bizId;

    @Schema(description = "业务单号")
    private String bizNo;

    @Schema(description = "备注")
    private String remark;

}
