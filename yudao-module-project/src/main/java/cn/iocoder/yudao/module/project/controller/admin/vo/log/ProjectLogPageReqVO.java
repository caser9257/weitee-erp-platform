package cn.iocoder.yudao.module.project.controller.admin.vo.log;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 操作日志分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProjectLogPageReqVO extends PageParam {

    @Schema(description = "项目编号", example = "1024")
    private Long projectId;

    @Schema(description = "任务编号", example = "1024")
    private Long taskId;

}
