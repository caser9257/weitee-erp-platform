package cn.iocoder.yudao.module.system.controller.admin.deptperson.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "Admin - Dept person tree node response")
@Data
public class DeptPersonTreeNodeRespVO {

    @Schema(description = "Node id", example = "dept-1024")
    private String id;

    @Schema(description = "Node type", example = "dept")
    private String type;

    @Schema(description = "Department id", example = "1024")
    private Long deptId;

    @Schema(description = "User id", example = "2048")
    private Long userId;

    @Schema(description = "Parent id", example = "1024")
    private Long parentId;

    @Schema(description = "Display label")
    private String label;

    @Schema(description = "Full path")
    private String fullPath;

    @Schema(description = "Sort order", example = "10")
    private Integer sort;

    @Schema(description = "Depth from root", example = "3")
    private Integer depth;

    @Schema(description = "Child department count", example = "2")
    private Integer childDeptCount;

    @Schema(description = "Direct user count", example = "5")
    private Integer directUserCount;

    @Schema(description = "Department count in subtree", example = "4")
    private Integer deptCount;

    @Schema(description = "User count in subtree", example = "12")
    private Integer userCount;

    @Schema(description = "Status", example = "0")
    private Integer status;

    @Schema(description = "Username")
    private String username;

    @Schema(description = "Nickname")
    private String nickname;

    @Schema(description = "Mobile")
    private String mobile;

    @Schema(description = "Children")
    private List<DeptPersonTreeNodeRespVO> children;

    @Schema(description = "Leaf department count", example = "1")
    private Integer leafDeptCount;

    @Schema(description = "Max depth of subtree", example = "4")
    private Integer maxDepth;

}
