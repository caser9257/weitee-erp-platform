package cn.weitee.erp.module.system.controller.admin.postlevel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 岗位层级汇总 Response VO")
@Data
public class PostLevelSummaryRespVO {

    @Schema(description = "岗位层级", example = "高级")
    private String level;

    @Schema(description = "岗位数量", example = "3")
    private Integer postCount;

    @Schema(description = "任职人数", example = "4")
    private Integer assignedUserCount;

    @Schema(description = "空岗数量", example = "1")
    private Integer vacancyCount;

}
