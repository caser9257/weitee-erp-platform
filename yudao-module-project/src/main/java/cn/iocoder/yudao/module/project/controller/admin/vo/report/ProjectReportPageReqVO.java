package cn.iocoder.yudao.module.project.controller.admin.vo.report;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 日报分页列表 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProjectReportPageReqVO extends PageParam {
    @Schema(description = "日报类型", example = "daily")
    private String type;
}
