package cn.iocoder.yudao.module.system.controller.admin.postlevel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "Admin - organization post tree node response")
@Data
public class PostOrgTreeNodeRespVO {

    @Schema(description = "Node ID", example = "dept-1")
    private String id;

    @Schema(description = "Node type", example = "dept")
    private String type;

    @Schema(description = "Dept ID", example = "1")
    private Long deptId;

    @Schema(description = "Post ID", example = "1024")
    private Long postId;

    @Schema(description = "Parent ID", example = "0")
    private Long parentId;

    @Schema(description = "Node label", example = "研发中心")
    private String label;

    @Schema(description = "Full path", example = "公司 / 研发中心 / 软件部")
    private String fullPath;

    @Schema(description = "Sort order", example = "10")
    private Integer sort;

    @Schema(description = "Tree depth", example = "3")
    private Integer depth;

    @Schema(description = "Direct child dept count", example = "2")
    private Integer childDeptCount;

    @Schema(description = "Direct post count", example = "3")
    private Integer directPostCount;

    @Schema(description = "Total post count", example = "9")
    private Integer postCount;

    @Schema(description = "Staff quota", example = "18")
    private Integer staffQuota;

    @Schema(description = "Assigned user count", example = "15")
    private Integer assignedUserCount;

    @Schema(description = "Status", example = "0")
    private Integer status;

    @Schema(description = "Post level", example = "中级")
    private String level;

    @Schema(description = "Children")
    private List<PostOrgTreeNodeRespVO> children;

}