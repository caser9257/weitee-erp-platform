package cn.weitee.erp.module.system.controller.admin.postlevel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 岗位层级总览 Response VO")
@Data
public class PostLevelDashboardRespVO {

    @Schema(description = "岗位总数", example = "15")
    private Integer postCount;

    @Schema(description = "在岗人数", example = "18")
    private Integer assignedUserCount;

    @Schema(description = "空岗数", example = "2")
    private Integer vacancyCount;

    @Schema(description = "关键岗位数", example = "4")
    private Integer keyPostCount;

    @Schema(description = "一人多岗人数", example = "3")
    private Integer partTimeUserCount;

    @Schema(description = "无后备岗位数", example = "1")
    private Integer noBackupCount;

    @Schema(description = "层级统计")
    private List<PostLevelSummaryRespVO> levelSummaries;

    @Schema(description = "风险提示")
    private List<String> riskTips;

}
