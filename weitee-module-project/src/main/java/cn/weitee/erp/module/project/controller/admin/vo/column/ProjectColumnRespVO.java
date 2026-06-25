package cn.weitee.erp.module.project.controller.admin.vo.column;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 项目列表 Response VO")
@Data
public class ProjectColumnRespVO {
    @Schema(description = "列表编号")
    private Long id;
    @Schema(description = "项目编号")
    private Long projectId;
    @Schema(description = "列表名称")
    private String name;
    @Schema(description = "排序")
    private Integer sort;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
