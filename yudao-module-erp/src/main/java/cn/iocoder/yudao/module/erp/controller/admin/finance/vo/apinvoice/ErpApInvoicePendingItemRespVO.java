package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apinvoice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 采购发票待匹配明细 Response VO")
@Data
public class ErpApInvoicePendingItemRespVO {

    @Schema(description = "应付台账编号", example = "31")
    private Long apStatementId;

    @Schema(description = "采购订单编号", example = "21")
    private Long sourceOrderId;

    @Schema(description = "采购订单号", example = "PO-001")
    private String sourceOrderNo;

    @Schema(description = "采购入库编号", example = "11")
    private Long sourcePurchaseInId;

    @Schema(description = "采购入库单号", example = "PI-001")
    private String sourcePurchaseInNo;

    @Schema(description = "采购入库明细编号", example = "1001")
    private Long sourcePurchaseInItemId;

    @Schema(description = "供应商编号", example = "201")
    private Long supplierId;

    @Schema(description = "产品编号", example = "301")
    private Long productId;

    @Schema(description = "产品名称", example = "螺丝")
    private String productName;

    @Schema(description = "采购入库时间")
    private LocalDateTime bizDate;

    @Schema(description = "入库数量", example = "10")
    private BigDecimal totalCount;

    @Schema(description = "已匹配数量", example = "8")
    private BigDecimal matchedCount;

    @Schema(description = "剩余可匹配数量", example = "2")
    private BigDecimal remainCount;

    @Schema(description = "入库金额（含税）", example = "100")
    private BigDecimal totalAmount;

    @Schema(description = "已匹配金额", example = "80")
    private BigDecimal matchedAmount;

    @Schema(description = "剩余可匹配金额", example = "20")
    private BigDecimal remainAmount;

}
