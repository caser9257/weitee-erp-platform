package cn.weitee.erp.module.infra.controller.admin.file.vo.version;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 文件版本回溯 Request VO")
@Data
public class FileVersionRollbackReqVO {

    @Schema(description = "文件ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "文件ID不能为空")
    private Long fileId;

    @Schema(description = "目标版本号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "版本号不能为空")
    private Integer version;

}