package cn.weitee.erp.module.infra.controller.admin.file.vo.log;

import cn.weitee.erp.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 文件操作日志分页 Request VO")
@Data
public class FileOperationLogPageReqVO extends PageParam {

    @Schema(description = "文件ID")
    private Long fileId;

    @Schema(description = "操作类型：UPLOAD-上传, DOWNLOAD-下载, DELETE-删除, VIEW-查看, RESTORE-恢复, ROLLBACK-回滚")
    private String operation;

    @Schema(description = "操作人ID")
    private Long userId;

    @Schema(description = "操作结果：0-成功, 1-失败")
    private Integer result;

    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;

}
