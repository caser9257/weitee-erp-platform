package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 应付台账导出 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpApStatementExportRespVO {

    @Schema(description = "台账编号", example = "AP-11-PI-001")
    @ExcelProperty("台账编号")
    private String statementNo;

    @Schema(description = "业务类型", example = "11")
    private Integer bizType;

    @Schema(description = "业务类型名称", example = "采购入库")
    @ExcelProperty("业务类型")
    private String bizTypeName;

    @Schema(description = "业务单号", example = "PI-001")
    @ExcelProperty("业务单号")
    private String bizNo;

    @Schema(description = "来源采购订单号", example = "PO-001")
    @ExcelProperty("来源采购订单号")
    private String sourceOrderNo;

    @Schema(description = "供应商名称", example = "华东供应商")
    @ExcelProperty("供应商名称")
    private String supplierName;

    @Schema(description = "结算账户名称", example = "中国银行")
    @ExcelProperty("结算账户")
    private String accountName;

    @Schema(description = "应付金额", example = "1200.00")
    @ExcelProperty("应付金额")
    private BigDecimal amount;

    @Schema(description = "已核销金额", example = "200.00")
    @ExcelProperty("已核销金额")
    private BigDecimal paidAmount;

    @Schema(description = "剩余金额", example = "1000.00")
    @ExcelProperty("剩余金额")
    private BigDecimal remainAmount;

    @Schema(description = "币种", example = "CNY")
    @ExcelProperty("币种")
    private String currencyCode;

    @Schema(description = "业务日期")
    @ExcelProperty("业务日期")
    private LocalDateTime bizDate;

    @Schema(description = "到期日期")
    @ExcelProperty("到期日期")
    private LocalDateTime dueDate;

    @Schema(description = "发票状态", example = "2")
    private Integer invoiceStatus;

    @Schema(description = "发票状态名称", example = "已收票")
    @ExcelProperty("发票状态")
    private String invoiceStatusName;

    @Schema(description = "发票号", example = "FP-20260424001")
    @ExcelProperty("发票号")
    private String invoiceNo;

    @Schema(description = "发票金额", example = "1200.00")
    @ExcelProperty("发票金额")
    private BigDecimal invoiceAmount;

    @Schema(description = "台账状态", example = "20")
    private Integer status;

    @Schema(description = "台账状态名称", example = "部分付款")
    @ExcelProperty("台账状态")
    private String statusName;

    @Schema(description = "备注", example = "采购入库自动生成")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建人", example = "admin")
    @ExcelProperty("创建人")
    private String creator;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
