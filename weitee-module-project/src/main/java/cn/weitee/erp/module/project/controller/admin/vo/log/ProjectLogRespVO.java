package cn.weitee.erp.module.project.controller.admin.vo.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 操作日志 Response VO")
@Data
public class ProjectLogRespVO {

    @Schema(description = "日志编号", example = "1024")
    private Long id;

    @Schema(description = "项目编号", example = "1024")
    private Long projectId;

    @Schema(description = "任务编号", example = "1024")
    private Long taskId;

    @Schema(description = "列表编号", example = "1024")
    private Long columnId;

    @Schema(description = "操作人编号", example = "1024")
    private Long userId;

    @Schema(description = "操作人姓名", example = "张三")
    private String userName;

    @Schema(description = "操作人头像")
    private String userAvatar;

    @Schema(description = "日志详情", example = "创建了任务")
    private String detail;

    @Schema(description = "变更记录")
    private Object record;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
