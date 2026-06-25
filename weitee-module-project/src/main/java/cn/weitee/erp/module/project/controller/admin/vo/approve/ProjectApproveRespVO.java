package cn.weitee.erp.module.project.controller.admin.vo.approve;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 审批 Response VO")
@Data
public class ProjectApproveRespVO {
    @Schema(description = "审批编号", example = "1024")
    private Long id;
    @Schema(description = "审批名称", example = "请假审批")
    private String name;
    @Schema(description = "审批状态", example = "0")
    private Integer status;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
