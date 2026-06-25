package cn.weitee.erp.module.erp.controller.admin.sale.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 市场执行台账 VO
 *
 * @author system
 */
@Schema(description = "市场执行台账 VO")
@Data
public class MarketLedgerVO {

    // ========== 项目信息 ==========
    @Schema(description = "项目编号", example = "1001")
    private Long projectId;

    @Schema(description = "项目编号", example = "PRJ-2026-001")
    private String projectNo;

    @Schema(description = "项目名称", example = "某某智能设备项目")
    private String projectName;

    @Schema(description = "当前生命周期阶段", example = "OUTBOUND")
    private String lifecycleStage;

    @Schema(description = "当前阻塞项")
    private String currentBlocker;

    @Schema(description = "当前待办角色")
    private String currentPendingRole;

    // ========== 合同信息 ==========
    @Schema(description = "合同编号", example = "2001")
    private Long contractId;

    @Schema(description = "合同编号", example = "CRM-CT-2026-012")
    private String contractNo;

    @Schema(description = "合同名称", example = "某某科技采购合同")
    private String contractName;

    @Schema(description = "合同审批状态", example = "2")
    private Integer contractAuditStatus;

    @Schema(description = "发货放行规则", example = "AFTER_PAYMENT")
    private String shipmentReleaseRule;

    @Schema(description = "开票触发条件", example = "AFTER_SHIPMENT")
    private String invoiceTrigger;

    @Schema(description = "收款规则", example = "BEFORE_SHIPMENT")
    private String collectionRule;

    // ========== 订单信息 ==========
    @Schema(description = "订单编号", example = "3001")
    private Long orderId;

    @Schema(description = "订单号", example = "SO-2026-042")
    private String orderNo;

    @Schema(description = "订单状态", example = "2")
    private Integer orderStatus;

    @Schema(description = "订单总金额", example = "128000.00")
    private BigDecimal orderTotalPrice;

    @Schema(description = "交期", example = "2026-06-30")
    private LocalDate deliveryDate;

    @Schema(description = "发货放行状态", example = "RELEASED")
    private String shipmentReleaseStatus;

    @Schema(description = "放行阻塞原因")
    private String shipmentReleaseReason;

    // ========== 收款信息 ==========
    @Schema(description = "已收款金额", example = "96000.00")
    private BigDecimal receivedAmount;

    @Schema(description = "应收金额", example = "128000.00")
    private BigDecimal receivableAmount;

    @Schema(description = "收款进度（%）", example = "75.00")
    private BigDecimal receiptProgress;

    // ========== 出库信息 ==========
    @Schema(description = "已出库数量", example = "8")
    private BigDecimal shippedCount;

    @Schema(description = "订单数量", example = "8")
    private BigDecimal orderCount;

    @Schema(description = "发货进度（%）", example = "100.00")
    private BigDecimal shipmentProgress;

    // ========== 开票信息 ==========
    @Schema(description = "已开票金额", example = "0.00")
    private BigDecimal invoicedAmount;

    @Schema(description = "开票状态", example = "NOT_INVOICED")
    private String invoiceStatus;

    // ========== 验收信息 ==========
    @Schema(description = "验收状态", example = "PENDING")
    private String acceptanceStatus;

}
