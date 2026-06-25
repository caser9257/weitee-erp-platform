package cn.weitee.erp.module.system.controller.admin.postlevel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "Admin - post level tree node response")
@Data
public class PostLevelTreeNodeRespVO {

    @Schema(description = "Node ID", example = "post-1")
    private String id;

    @Schema(description = "Post ID", example = "1")
    private Long postId;

    @Schema(description = "Node label", example = "计划经理")
    private String label;

    @Schema(description = "Node type", example = "post")
    private String type;

    @Schema(description = "Post level", example = "高级")
    private String level;

    @Schema(description = "Dept ID", example = "103")
    private Long deptId;

    @Schema(description = "Assigned user count", example = "2")
    private Integer assignedUserCount;

    @Schema(description = "Staff quota", example = "3")
    private Integer staffQuota;

    @Schema(description = "Vacancy count", example = "1")
    private Integer vacancyCount;

    @Schema(description = "Primary user name", example = "张三")
    private String primaryUserName;

    @Schema(description = "Preview user names")
    private List<String> previewUserNames;

    @Schema(description = "Children")
    private List<PostLevelTreeNodeRespVO> children;

}
