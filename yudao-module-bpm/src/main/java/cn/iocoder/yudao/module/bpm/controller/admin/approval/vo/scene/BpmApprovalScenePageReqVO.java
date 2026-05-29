package cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.scene;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 审批场景分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmApprovalScenePageReqVO extends PageParam {

    @Schema(description = "场景名称", example = "付款单提交审批")
    private String name;

    @Schema(description = "模块编码", example = "erp_finance")
    private String moduleCode;

    @Schema(description = "业务类型", example = "payment")
    private String bizType;

    @Schema(description = "状态", example = "1")
    private Integer status;

}
