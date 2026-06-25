package cn.weitee.erp.module.infra.controller.admin.file.vo.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 文件操作日志 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FileOperationLogRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "文件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long fileId;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "操作类型：UPLOAD-上传, DOWNLOAD-下载, DELETE-删除, VIEW-查看, RESTORE-恢复, ROLLBACK-回滚")
    private String operation;

    @Schema(description = "操作人ID")
    private Long userId;

    @Schema(description = "操作人名称")
    private String userName;

    @Schema(description = "操作IP")
    private String ip;

    @Schema(description = "浏览器信息")
    private String userAgent;

    @Schema(description = "操作说明")
    private String description;

    @Schema(description = "操作结果：0-成功, 1-失败")
    private Integer result;

    @Schema(description = "失败原因")
    private String failReason;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
