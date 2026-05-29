package cn.iocoder.yudao.module.system.controller.admin.dept.vo.post;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 岗位分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class PostPageReqVO extends PageParam {

    @Schema(description = "岗位编码，模糊匹配", example = "plan_manager")
    private String code;

    @Schema(description = "岗位名称，模糊匹配", example = "计划经理")
    private String name;

    @Schema(description = "岗位层级", example = "高级")
    private String level;

    @Schema(description = "所属部门编号", example = "103")
    private Long deptId;

    @Schema(description = "是否关键岗位", example = "true")
    private Boolean keyPosition;

    @Schema(description = "岗位状态", example = "0")
    private Integer status;

}