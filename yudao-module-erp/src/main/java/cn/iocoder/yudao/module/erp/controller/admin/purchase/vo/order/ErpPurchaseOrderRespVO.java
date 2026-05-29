package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order;

import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 采购订单 Response VO")
@Data
@Accessors(chain = true)
@ExcelIgnoreUnannotated
public class ErpPurchaseOrderRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17386")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "采购订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "CG001")
    @ExcelProperty("采购订单号")
    private String no;

    @Schema(description = "采购状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @ExcelProperty("采购状态")
    private Integer status;

    @Schema(description = "BPM 流程实例编号", example = "6f4d1f0c-xxx")
    private String processInstanceId;

    @Schema(description = "最近一次驳回原因")
    private String lastRejectReason;

    @Schema(description = "最近一次驳回时间")
    private LocalDateTime lastRejectTime;

    @Schema(description = "最近一次驳回人编号")
    private Long lastRejectUserId;

    @Schema(description = "来源类型", example = "MRP")
    private String sourceType;

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1724")
    private Long supplierId;

    @Schema(description = "项目编号", example = "1")
    private Long projectId;

    @Schema(description = "供应商名称", example = "芋道")
    @ExcelProperty("供应商名称")
    private String supplierName;

    @Schema(description = "结算账户编号", example = "31189")
    @ExcelProperty("结算账户编号")
    private Long accountId;

    @Schema(description = "采购时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("采购时间")
    private LocalDateTime orderTime;

    @Schema(description = "合计数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "15663")
    @ExcelProperty("合计数量")
    private BigDecimal totalCount;

    @Schema(description = "最终合计价格", requiredMode = Schema.RequiredMode.REQUIRED, example = "24906")
    @ExcelProperty("最终合计价格")
    private BigDecimal totalPrice;

    @Schema(description = "合计产品价格，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "7127")
    private BigDecimal totalProductPrice;

    @Schema(description = "合计税额，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "7127")
    private BigDecimal totalTaxPrice;

    @Schema(description = "优惠率，百分比", requiredMode = Schema.RequiredMode.REQUIRED, example = "99.88")
    private BigDecimal discountPercent;

    @Schema(description = "优惠金额，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "7127")
    private BigDecimal discountPrice;

    @Schema(description = "订金金额，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "7127")
    private BigDecimal depositPrice;

    @Schema(description = "附件地址", example = "https://www.iocoder.cn")
    @ExcelProperty("附件地址")
    private String fileUrl;

    @Schema(description = "备注", example = "你猜")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建人", example = "1")
    private String creator;

    @Schema(description = "创建人名称", example = "采购下单员")
    private String creatorName;

    @Schema(description = "业务归属 / 来源销售业务员", example = "销售组长")
    private String businessOwnerName;

    @Schema(description = "来源销售订单号", example = "XSDD20260408000001")
    @ExcelProperty("来源销售订单号")
    private String sourceOrderNos;

    @Schema(description = "是否存在待处理采购入库", example = "true")
    private Boolean hasPendingPurchaseIn;

    @Schema(description = "待处理采购入库单编号", example = "1024")
    private Long pendingPurchaseInId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "订单项列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Item> items;

    @Schema(description = "驳回记录")
    private List<ErpPurchaseOrderRejectLogRespVO> rejectLogs;

    @Schema(description = "审批流转记录")
    private List<ErpPurchaseOrderAuditLogRespVO> auditLogs;

    @Schema(description = "产品信息", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("产品信息")
    private String productNames;

    @Schema(description = "采购入库数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    private BigDecimal inCount;

    @Schema(description = "采购退货数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    private BigDecimal returnCount;

    @Data
    @Accessors(chain = true)
    public static class Item {

        @Schema(description = "订单项编号", example = "11756")
        private Long id;

        @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        private Long productId;

        @Schema(description = "项目编号", example = "1")
        private Long projectId;

        @Schema(description = "产品单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        private Long productUnitId;

        @Schema(description = "产品单价", example = "100.00")
        private BigDecimal productPrice;

        @Schema(description = "工程费", example = "2000.00")
        private BigDecimal engineeringFee;

        @Schema(description = "计价BOM编号", example = "1001")
        private Long pricingBomId;

        @Schema(description = "计价BOM版本", example = "V1.0")
        private String pricingBomVersion;

        @Schema(description = "产品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        @NotNull(message = "产品数量不能为空")
        private BigDecimal count;

        @Schema(description = "税率，百分比", example = "99.88")
        private BigDecimal taxPercent;

        @Schema(description = "税额，单位：元", example = "100.00")
        private BigDecimal taxPrice;

        @Schema(description = "产品金额", example = "100.00")
        private BigDecimal totalProductPrice;

        @Schema(description = "含税金额", example = "100.00")
        private BigDecimal totalPrice;

        @Schema(description = "备注", example = "随便")
        private String remark;

        @Schema(description = "采购入库数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        private BigDecimal inCount;

        @Schema(description = "采购退货数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        private BigDecimal returnCount;

        @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "巧克力")
        private String productName;

        @Schema(description = "产品条码", requiredMode = Schema.RequiredMode.REQUIRED, example = "A9985")
        private String productBarCode;

        @Schema(description = "产品规格", example = "QFN-16")
        private String productStandard;

        @Schema(description = "产品单位名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "盒")
        private String productUnitName;

        @Schema(description = "项目名称", example = "华东交付项目")
        private String projectName;

        @Schema(description = "含税单价", example = "113.00")
        private BigDecimal taxIncludedPrice;

        @Schema(description = "交货日期")
        private LocalDateTime deliveryDate;

        @Schema(description = "付款关联金额", example = "500.00")
        private BigDecimal paymentAllocatedAmount;

        @Schema(description = "关联数量", example = "100.00")
        private BigDecimal relatedCount;

        @Schema(description = "开票数量", example = "80.00")
        private BigDecimal invoicedCount;

        @Schema(description = "审核人", example = "王主管")
        private String auditorName;

        @Schema(description = "库存数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        private BigDecimal stockCount;
    }
}
