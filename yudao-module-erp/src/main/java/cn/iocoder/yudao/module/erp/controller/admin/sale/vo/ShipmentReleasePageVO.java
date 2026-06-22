package cn.iocoder.yudao.module.erp.controller.admin.sale.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 发货放行分页响应 VO
 *
 * @author system
 */
@Schema(description = "ERP - 发货放行分页响应 VO")
@Data
public class ShipmentReleasePageVO {

    @Schema(description = "订单编号", example = "1024")
    private Long orderId;

    @Schema(description = "销售订单号", example = "SO20240001")
    private String orderNo;

    @Schema(description = "项目编号", example = "1024")
    private Long projectId;

    @Schema(description = "项目编号", example = "PRJ20240001")
    private String projectNo;

    @Schema(description = "项目名称", example = "示例项目")
    private String projectName;

    @Schema(description = "客户编号", example = "1024")
    private Long customerId;

    @Schema(description = "客户名称", example = "示例客户")
    private String customerName;

    @Schema(description = "合同编号", example = "1024")
    private Long contractId;

    @Schema(description = "合同编号", example = "CON20240001")
    private String contractNo;

    @Schema(description = "合同名称", example = "示例合同")
    private String contractName;

    @Schema(description = "销售员编号", example = "1024")
    private Long saleUserId;

    @Schema(description = "销售员名称", example = "张三")
    private String saleUserName;

    @Schema(description = "订单总金额", example = "10000.00")
    private BigDecimal orderTotalPrice;

    @Schema(description = "已收金额", example = "5000.00")
    private BigDecimal receivedAmount;

    @Schema(description = "应收金额", example = "5000.00")
    private BigDecimal receivableAmount;

    @Schema(description = "发货放行状态", example = "RELEASED")
    private String releaseStatus;

    @Schema(description = "放行阻塞原因")
    private String shipmentReleaseReason;

    @Schema(description = "放行规则", example = "SIGN_AND_SHIP")
    private String releaseRule;

    @Schema(description = "交期", example = "2024-12-31")
    private LocalDate deliveryDate;

    @Schema(description = "阻塞原因列表")
    private List<String> blockerReasons;

    @Schema(description = "待办角色")
    private String pendingRole;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
