package cn.weitee.erp.module.bpm.controller.admin.approval.vo.delegation;

import cn.weitee.erp.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 审批委托分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmApprovalDelegationPageReqVO extends PageParam {

    @Schema(description = "委托人ID", example = "1024")
    private Long userId;

    @Schema(description = "代理人ID", example = "1025")
    private Long delegateUserId;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
