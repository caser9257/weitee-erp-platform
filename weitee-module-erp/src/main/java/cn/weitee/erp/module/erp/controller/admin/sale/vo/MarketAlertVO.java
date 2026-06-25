package cn.weitee.erp.module.erp.controller.admin.sale.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 市场预警 VO
 *
 * @author system
 */
@Schema(description = "市场预警 VO")
@Data
public class MarketAlertVO {

    @Schema(description = "预警ID", example = "1")
    private Long id;

    @Schema(description = "规则编码", example = "RECEIPT_OVERDUE")
    private String ruleCode;

    @Schema(description = "规则名称", example = "收款逾期预警")
    private String ruleName;

    @Schema(description = "预警级别", example = "WARNING")
    private String level;

    @Schema(description = "项目编号", example = "1001")
    private Long projectId;

    @Schema(description = "项目编号", example = "PRJ-2026-001")
    private String projectNo;

    @Schema(description = "项目名称", example = "某某智能设备项目")
    private String projectName;

    @Schema(description = "订单编号", example = "2001")
    private Long orderId;

    @Schema(description = "订单号", example = "SO-2026-042")
    private String orderNo;

    @Schema(description = "客户名称", example = "某某科技有限公司")
    private String customerName;

    @Schema(description = "预警内容", example = "订单交期已过30天，仍未收到货款")
    private String content;

    @Schema(description = "触发时间")
    private LocalDateTime triggerTime;

    @Schema(description = "是否已处理", example = "false")
    private Boolean handled;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "处理人")
    private String handleUser;

}
