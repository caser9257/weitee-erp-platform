package cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 审批统计数据 Response VO
 */
@Data
@Schema(description = "审批统计数据 Response VO")
public class ApprovalStatisticsRespVO {

    @Schema(description = "总审批数", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long totalCount;

    @Schema(description = "审批中数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Long processingCount;

    @Schema(description = "已通过数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "60")
    private Long approvedCount;

    @Schema(description = "已拒绝数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "15")
    private Long rejectedCount;

    @Schema(description = "已撤回数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Long cancelledCount;

}
