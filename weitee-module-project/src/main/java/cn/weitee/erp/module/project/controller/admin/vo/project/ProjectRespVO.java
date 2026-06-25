package cn.weitee.erp.module.project.controller.admin.vo.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 项目 Response VO")
@Data
public class ProjectRespVO {
    @Schema(description = "项目编号", example = "1024")
    private Long id;
    @Schema(description = "项目名称", example = "测试项目")
    private String name;
    @Schema(description = "项目描述")
    private String description;
    @Schema(description = "负责人用户编号")
    private Long ownerUserId;
    @Schema(description = "负责人姓名")
    private String ownerUserName;
    @Schema(description = "是否个人项目")
    private Boolean personal;
    @Schema(description = "归档时间")
    private LocalDateTime archivedAt;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
