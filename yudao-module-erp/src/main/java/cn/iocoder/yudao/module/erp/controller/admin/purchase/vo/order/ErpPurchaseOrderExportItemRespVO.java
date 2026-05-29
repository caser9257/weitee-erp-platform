package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order;

import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 采购订单导出明细 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpPurchaseOrderExportItemRespVO {

    @ExcelProperty("采购订单号")
    private String no;

    @ExcelProperty("供应商名称")
    private String supplierName;

    @ExcelProperty("来源销售订单号")
    private String sourceOrderNos;

    @ExcelProperty("来源类型")
    private String sourceType;

    @ExcelProperty("创建人")
    private String creatorName;

    @ExcelProperty("业务归属")
    private String businessOwnerName;

    @ExcelProperty("采购时间")
    private LocalDateTime orderTime;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("产品名称")
    private String productName;

    @ExcelProperty("产品条码")
    private String productBarCode;

    @ExcelProperty("规格型号")
    private String productStandard;

    @ExcelProperty("单位")
    private String productUnitName;

    @ExcelProperty("项目名称")
    private String projectName;

    @ExcelProperty("数量")
    private BigDecimal count;

    @ExcelProperty("单价")
    private BigDecimal productPrice;

    @ExcelProperty("含税单价")
    private BigDecimal taxIncludedPrice;

    @ExcelProperty("工程费")
    private BigDecimal engineeringFee;

    @ExcelProperty("税率")
    private BigDecimal taxPercent;

    @ExcelProperty("税额")
    private BigDecimal taxPrice;

    @ExcelProperty("含税金额")
    private BigDecimal totalPrice;

    @ExcelProperty("已入库数量")
    private BigDecimal inCount;

    @ExcelProperty("交货日期")
    private LocalDateTime deliveryDate;

    @ExcelProperty("付款关联金额")
    private BigDecimal paymentAllocatedAmount;

    @ExcelProperty("关联数量")
    private BigDecimal relatedCount;

    @ExcelProperty("开票数量")
    private BigDecimal invoicedCount;

    @ExcelProperty("审核人")
    private String auditorName;

    @ExcelProperty("已退货数量")
    private BigDecimal returnCount;

    @ExcelProperty("备注")
    private String remark;
}
