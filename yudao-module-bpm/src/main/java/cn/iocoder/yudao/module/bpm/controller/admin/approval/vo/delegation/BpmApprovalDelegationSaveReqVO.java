package cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.delegation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.Date;

@Schema(description = "管理后台 - 审批委托新增/修改 Request VO")
@Data
public class BpmApprovalDelegationSaveReqVO {

    @Schema(description = "委托编号", example = "1")
    private Long id;

    @Schema(description = "委托人ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "委托人ID不能为空")
    private Long userId;

    @Schema(description = "代理人ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1025")
    @NotNull(message = "代理人ID不能为空")
    private Long delegateUserId;

    @Schema(description = "委托开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "委托开始时间不能为空")
    private Date startTime;

    @Schema(description = "委托结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "委托结束时间不能为空")
    private Date endTime;

    @Schema(description = "场景编码（为空表示所有场景）", example = "erp.sale.order.submit")
    private String sceneCode;

    @Schema(description = "委托原因", example = "出差期间")
    private String reason;

}
