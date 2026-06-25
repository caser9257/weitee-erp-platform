package cn.weitee.erp.module.erp.controller.admin.sale.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 市场执行台账统计 VO
 *
 * @author system
 */
@Schema(description = "市场执行台账统计 VO")
@Data
public class MarketLedgerStatsVO {

    @Schema(description = "总订单数", example = "128")
    private Integer totalOrderCount;

    @Schema(description = "总金额", example = "3200000.00")
    private BigDecimal totalOrderAmount;

    @Schema(description = "已收款金额", example = "1800000.00")
    private BigDecimal totalReceivedAmount;

    @Schema(description = "待放行订单数", example = "12")
    private Integer pendingReleaseCount;

    @Schema(description = "已出库订单数", example = "96")
    private Integer shippedOrderCount;

    @Schema(description = "待开票订单数", example = "24")
    private Integer pendingInvoiceCount;

    @Schema(description = "待验收订单数", example = "8")
    private Integer pendingAcceptanceCount;

    @Schema(description = "异常订单数", example = "5")
    private Integer abnormalOrderCount;

}
