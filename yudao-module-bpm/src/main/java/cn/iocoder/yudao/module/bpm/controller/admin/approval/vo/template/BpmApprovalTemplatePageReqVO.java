package cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.template;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 审批模板分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmApprovalTemplatePageReqVO extends PageParam {

    @Schema(description = "模板名称", example = "请假审批")
    private String name;

    @Schema(description = "模板分类", example = "OA")
    private String category;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
