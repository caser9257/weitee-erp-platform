package cn.weitee.erp.module.erp.controller.admin.finance.vo.apestimate;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 暂估单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpApEstimateRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "暂估单号", example = "ZG202604270001")
    @ExcelProperty("暂估单号")
    private String estimateNo;

    @Schema(description = "暂估月份", example = "2026-04")
    @ExcelProperty("暂估月份")
    private String estimateMonth;

    @Schema(description = "来源入库单编号", example = "1")
    private Long sourcePurchaseInId;

    @Schema(description = "来源入库单号", example = "RK202604270001")
    @ExcelProperty("来源入库单号")
    private String sourcePurchaseInNo;

    @Schema(description = "来源采购订单编号", example = "CGDD202604270001")
    @ExcelProperty("来源采购订单编号")
    private String sourceOrderNo;

    @Schema(description = "来源采购订单ID", example = "1")
    private Long sourceOrderId;

    @Schema(description = "供应商编号", example = "1")
    private Long supplierId;

    @Schema(description = "供应商名称", example = "广州供应商")
    @ExcelProperty("供应商名称")
    private String supplierName;

    @Schema(description = "结算账户编号", example = "1")
    private Long accountId;

    @Schema(description = "结算账户名称", example = "应付账户")
    @ExcelProperty("结算账户")
    private String accountName;

    @Schema(description = "来源未税金额，单位：元", example = "10000")
    @ExcelProperty("来源未税金额")
    private BigDecimal sourceAmount;

    @Schema(description = "暂估金额，单位：元", example = "10000")
    @ExcelProperty("暂估金额")
    private BigDecimal amount;

    @Schema(description = "暂估状态", example = "10")
    private Integer status;

    @Schema(description = "暂估状态名称", example = "待确认")
    @ExcelProperty("暂估状态")
    private String statusName;

    @Schema(description = "闭环状态", example = "20")
    private Integer closureStatus;

    @Schema(description = "闭环状态名称", example = "已确认待收票")
    private String closureStatusName;

    @Schema(description = "确认人编号", example = "1")
    private Long confirmUserId;

    @Schema(description = "确认时间")
    private LocalDateTime confirmTime;

    @Schema(description = "冲回人编号", example = "1")
    private Long reverseUserId;

    @Schema(description = "冲回时间")
    private LocalDateTime reverseTime;

    @Schema(description = "冲回类型", example = "20")
    private Integer reverseType;

    @Schema(description = "冲回类型名称", example = "收票冲回")
    private String reverseTypeName;

    @Schema(description = "冲回来源编号", example = "1")
    private Long reverseSourceId;

    @Schema(description = "冲回来源单号", example = "INV-20260428-001")
    private String reverseSourceNo;

    @Schema(description = "冲回备注", example = "收票自动回冲")
    private String reverseRemark;

    @Schema(description = "备注", example = "月末暂估")
    private String remark;

    @Schema(description = "创建人", example = "1")
    private String creator;

    @Schema(description = "创建人名称", example = "张三")
    private String creatorName;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "来源应付台账编号", example = "31")
    private Long sourceStatementId;

    @Schema(description = "来源应付台账号", example = "AP-11-PI-001")
    private String sourceStatementNo;

    @Schema(description = "来源应付台账状态", example = "10")
    private Integer sourceStatementStatus;

    @Schema(description = "来源应付台账状态名称", example = "未付款")
    private String sourceStatementStatusName;

    @Schema(description = "来源应付台账收票状态", example = "2")
    private Integer sourceStatementInvoiceStatus;

    @Schema(description = "来源应付台账收票状态名称", example = "已收票")
    private String sourceStatementInvoiceStatusName;

    @Schema(description = "来源应付台账发票号摘要", example = "INV-20260428-001")
    private String sourceStatementInvoiceNo;

    @Schema(description = "来源应付台账发票金额摘要", example = "95.00")
    private BigDecimal sourceStatementInvoiceAmount;

    @Schema(description = "暂估明细列表")
    private List<Item> items;

    @Data
    public static class Item {

        @Schema(description = "编号", example = "1")
        private Long id;

        @Schema(description = "来源采购入库明细编号", example = "1")
        private Long sourcePurchaseInItemId;

        @Schema(description = "来源采购订单项编号", example = "1")
        private Long sourceOrderItemId;

        @Schema(description = "来源采购订单号", example = "CGDD202604270001")
        private String sourceOrderNo;

        @Schema(description = "产品编号", example = "1")
        private Long productId;

        @Schema(description = "产品名称", example = "螺丝")
        private String productName;

        @Schema(description = "产品条码", example = "A001")
        private String productBarCode;

        @Schema(description = "产品单位", example = "个")
        private String productUnitName;

        @Schema(description = "仓库编号", example = "1")
        private Long warehouseId;

        @Schema(description = "仓库名称", example = "原材料仓")
        private String warehouseName;

        @Schema(description = "项目编号", example = "1")
        private Long projectId;

        @Schema(description = "项目名称", example = "研发项目A")
        private String projectName;

        @Schema(description = "数量", example = "10")
        private BigDecimal count;

        @Schema(description = "来源未税金额，单位：元", example = "1000")
        private BigDecimal sourceAmount;

        @Schema(description = "税额，单位：元", example = "130")
        private BigDecimal taxAmount;

        @Schema(description = "暂估金额，单位：元", example = "1000")
        private BigDecimal amount;

        @Schema(description = "备注", example = "随便")
        private String remark;

    }

}
