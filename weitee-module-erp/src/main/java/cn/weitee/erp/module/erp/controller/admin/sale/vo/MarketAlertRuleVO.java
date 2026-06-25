package cn.weitee.erp.module.erp.controller.admin.sale.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 市场预警规则 VO
 *
 * @author system
 */
@Schema(description = "市场预警规则 VO")
@Data
public class MarketAlertRuleVO {

    @Schema(description = "规则编码", example = "RECEIPT_OVERDUE")
    private String code;

    @Schema(description = "规则名称", example = "收款逾期预警")
    private String name;

    @Schema(description = "规则描述", example = "订单交期后30天仍未收款")
    private String description;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "阈值（天数）", example = "30")
    private Integer thresholdDays;

    @Schema(description = "预警级别", example = "WARNING")
    private String level;

}
