package cn.iocoder.yudao.module.project.controller.admin.vo.meeting;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 会议分页列表 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProjectMeetingPageReqVO extends PageParam {
    @Schema(description = "会议名称", example = "周会")
    private String name;
}
