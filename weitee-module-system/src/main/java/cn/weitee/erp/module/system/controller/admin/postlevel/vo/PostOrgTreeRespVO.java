package cn.weitee.erp.module.system.controller.admin.postlevel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "Admin - organization post tree response")
@Data
public class PostOrgTreeRespVO {

    @Schema(description = "Dept count", example = "12")
    private Integer deptCount;

    @Schema(description = "Post count", example = "38")
    private Integer postCount;

    @Schema(description = "Staff quota", example = "44")
    private Integer staffQuota;

    @Schema(description = "Assigned user count", example = "40")
    private Integer assignedUserCount;

    @Schema(description = "Max depth", example = "4")
    private Integer maxDepth;

    @Schema(description = "Tree")
    private List<PostOrgTreeNodeRespVO> tree;

}