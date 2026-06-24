package cn.iocoder.yudao.module.infra.controller.admin.file.vo.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 文件权限授权 Request VO")
@Data
public class FilePermissionSaveReqVO {

    @Schema(description = "文件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "文件ID不能为空")
    private Long fileId;

    @Schema(description = "授权类型：USER-用户, ROLE-角色, DEPT-部门", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "授权类型不能为空")
    private String grantType;

    @Schema(description = "授权目标ID（用户ID/角色ID/部门ID）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "授权目标ID不能为空")
    private Long grantTargetId;

    @Schema(description = "授权目标名称")
    private String grantTargetName;

    @Schema(description = "权限类型：VIEW-查看, DOWNLOAD-下载, EDIT-编辑, DELETE-删除, SHARE-分享", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "权限类型不能为空")
    private String permissions;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "备注")
    private String remark;

}
