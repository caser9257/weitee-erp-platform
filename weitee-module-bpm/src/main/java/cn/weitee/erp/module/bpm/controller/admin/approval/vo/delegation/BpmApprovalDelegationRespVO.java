package cn.weitee.erp.module.bpm.controller.admin.approval.vo.delegation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Schema(description = "管理后台 - 审批委托 Response VO")
@Data
public class BpmApprovalDelegationRespVO {

    @Schema(description = "委托编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "委托人ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long userId;

    @Schema(description = "代理人ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1025")
    private Long delegateUserId;

    @Schema(description = "委托开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date startTime;

    @Schema(description = "委托结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date endTime;

    @Schema(description = "场景编码（为空表示所有场景）", example = "erp.sale.order.submit")
    private String sceneCode;

    @Schema(description = "委托原因", example = "出差期间")
    private String reason;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
