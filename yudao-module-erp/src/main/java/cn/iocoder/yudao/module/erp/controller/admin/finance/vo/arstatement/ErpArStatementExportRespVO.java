package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.arstatement;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 应收台账导出 Response VO
 *
 * @author system
 */
@Schema(description = "管理后台 - ERP 应收台账导出 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpArStatementExportRespVO {

    @Schema(description = "台账编号", example = "AR20240001")
    @ExcelProperty("台账编号")
    private String statementNo;

    @Schema(description = "业务类型", example = "21")
    private Integer bizType;

    @Schema(description = "业务类型名称", example = "销售出库")
    @ExcelProperty("业务类型")
    private String bizTypeName;

    @Schema(description = "业务单号", example = "SO20240001")
    @ExcelProperty("业务单号")
    private String bizNo;

    @Schema(description = "来源销售订单号", example = "SO20240001")
    @ExcelProperty("来源销售订单号")
    private String sourceOrderNo;

    @Schema(description = "客户名称", example = "示例客户")
    @ExcelProperty("客户名称")
    private String customerName;

    @Schema(description = "结算账户名称", example = "基本户")
    @ExcelProperty("结算账户")
    private String accountName;

    @Schema(description = "应收金额", example = "10000.00")
    @ExcelProperty("应收金额")
    private BigDecimal amount;

    @Schema(description = "已收金额", example = "5000.00")
    @ExcelProperty("已收金额")
    private BigDecimal receivedAmount;

    @Schema(description = "剩余金额", example = "5000.00")
    @ExcelProperty("剩余金额")
    private BigDecimal remainAmount;

    @Schema(description = "币种", example = "CNY")
    @ExcelProperty("币种")
    private String currencyCode;

    @Schema(description = "业务日期")
    @ExcelProperty("业务日期")
    private LocalDate bizDate;

    @Schema(description = "到期日期")
    @ExcelProperty("到期日期")
    private LocalDate dueDate;

    @Schema(description = "发票状态", example = "0")
    private Integer invoiceStatus;

    @Schema(description = "发票状态名称", example = "未开票")
    @ExcelProperty("发票状态")
    private String invoiceStatusName;

    @Schema(description = "发票号")
    @ExcelProperty("发票号")
    private String invoiceNo;

    @Schema(description = "已开票金额", example = "0")
    @ExcelProperty("发票金额")
    private BigDecimal invoiceAmount;

    @Schema(description = "台账状态", example = "0")
    private Integer status;

    @Schema(description = "台账状态名称", example = "待收")
    @ExcelProperty("台账状态")
    private String statusName;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建人")
    @ExcelProperty("创建人")
    private String creator;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
