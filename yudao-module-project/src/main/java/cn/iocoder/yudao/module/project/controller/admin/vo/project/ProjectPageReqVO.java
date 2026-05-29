package cn.iocoder.yudao.module.project.controller.admin.vo.project;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 项目分页列表 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProjectPageReqVO extends PageParam {
    @Schema(description = "项目名称", example = "测试")
    private String name;
    @Schema(description = "是否归档", example = "false")
    private Boolean archived;
}
