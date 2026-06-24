package cn.iocoder.yudao.module.infra.controller.admin.file.vo.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 文件权限 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FilePermissionRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "文件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long fileId;

    @Schema(description = "授权类型：USER-用户, ROLE-角色, DEPT-部门")
    private String grantType;

    @Schema(description = "授权目标ID（用户ID/角色ID/部门ID）")
    private Long grantTargetId;

    @Schema(description = "授权目标名称")
    private String grantTargetName;

    @Schema(description = "权限类型：VIEW-查看, DOWNLOAD-下载, EDIT-编辑, DELETE-删除, SHARE-分享")
    private String permissions;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "授权人ID")
    private Long grantUserId;

    @Schema(description = "授权人名称")
    private String grantUserName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
