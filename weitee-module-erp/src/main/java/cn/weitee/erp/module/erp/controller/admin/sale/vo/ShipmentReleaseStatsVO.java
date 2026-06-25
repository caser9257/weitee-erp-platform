package cn.weitee.erp.module.erp.controller.admin.sale.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 发货放行统计 VO
 *
 * @author system
 */
@Schema(description = "ERP - 发货放行统计 VO")
@Data
public class ShipmentReleaseStatsVO {

    @Schema(description = "总订单数", example = "100")
    private Long totalCount;

    @Schema(description = "阻塞订单数", example = "10")
    private Long blockedCount;

    @Schema(description = "待财务审核订单数", example = "20")
    private Long financeReviewCount;

    @Schema(description = "已放行订单数", example = "70")
    private Long releasedCount;

}
