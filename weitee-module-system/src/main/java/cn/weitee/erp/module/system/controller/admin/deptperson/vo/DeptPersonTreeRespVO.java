package cn.weitee.erp.module.system.controller.admin.deptperson.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "Admin - Dept person tree response")
@Data
public class DeptPersonTreeRespVO {

    @Schema(description = "Department count", example = "12")
    private Integer deptCount;

    @Schema(description = "User count", example = "120")
    private Integer userCount;

    @Schema(description = "Leaf department count", example = "8")
    private Integer leafDeptCount;

    @Schema(description = "Unassigned user count", example = "3")
    private Integer unassignedUserCount;

    @Schema(description = "Max depth", example = "5")
    private Integer maxDepth;

    @Schema(description = "Tree data")
    private List<DeptPersonTreeNodeRespVO> tree;

}
