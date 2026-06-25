package cn.weitee.erp.module.erp.controller.admin.finance.vo.apstatement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 可核销应付 Response VO")
@Data
public class ErpApStatementPaymentEnableRespVO {

    @Schema(description = "台账编号", example = "1")
    private Long id;

    @Schema(description = "台账单号", example = "AP-11-PI-001")
    private String statementNo;

    @Schema(description = "业务类型", example = "11")
    private Integer bizType;

    @Schema(description = "业务编号", example = "11")
    private Long bizId;

    @Schema(description = "业务单号", example = "PI-001")
    private String bizNo;

    @Schema(description = "来源采购订单号", example = "PO-001")
    private String sourceOrderNo;

    @Schema(description = "供应商编号", example = "1")
    private Long supplierId;

    @Schema(description = "供应商名称", example = "华东供应商")
    private String supplierName;

    @Schema(description = "结算账户编号", example = "1")
    private Long accountId;

    @Schema(description = "结算账户名称", example = "中国银行")
    private String accountName;

    @Schema(description = "应付金额", example = "120.00")
    private BigDecimal amount;

    @Schema(description = "已核销金额", example = "20.00")
    private BigDecimal paidAmount;

    @Schema(description = "剩余金额", example = "100.00")
    private BigDecimal remainAmount;

    @Schema(description = "币种", example = "CNY")
    private String currencyCode;

    @Schema(description = "业务日期")
    private LocalDateTime bizDate;

    @Schema(description = "到期日期")
    private LocalDateTime dueDate;

    @Schema(description = "台账状态", example = "10")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
