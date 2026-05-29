package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 应付台账 Response VO")
@Data
public class ErpApStatementRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "台账编号", example = "AP-11-PI-001")
    private String statementNo;

    @Schema(description = "业务类型", example = "11")
    private Integer bizType;

    @Schema(description = "业务编号", example = "11")
    private Long bizId;

    @Schema(description = "业务单号", example = "PI-001")
    private String bizNo;

    @Schema(description = "来源采购订单编号", example = "100")
    private Long sourceOrderId;

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

    @Schema(description = "发票状态", example = "0")
    private Integer invoiceStatus;

    @Schema(description = "发票号", example = "FP-001")
    private String invoiceNo;

    @Schema(description = "发票金额", example = "120.00")
    private BigDecimal invoiceAmount;

    @Schema(description = "状态", example = "10")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建人", example = "1")
    private String creator;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "台账明细列表")
    private List<Item> items;

    @Data
    public static class Item {

        @Schema(description = "编号", example = "1")
        private Long id;

        @Schema(description = "明细类型", example = "10")
        private Integer itemType;

        @Schema(description = "来源类型", example = "11")
        private Integer refType;

        @Schema(description = "来源编号", example = "11")
        private Long refId;

        @Schema(description = "来源单号", example = "PI-001")
        private String refNo;

        @Schema(description = "变动金额", example = "120.00")
        private BigDecimal amount;

        @Schema(description = "变动后已付金额", example = "20.00")
        private BigDecimal afterPaidAmount;

        @Schema(description = "变动后剩余金额", example = "100.00")
        private BigDecimal afterRemainAmount;

        @Schema(description = "备注", example = "create")
        private String remark;

        @Schema(description = "操作人名称", example = "张三")
        private String operatorName;

        @Schema(description = "创建时间")
        private LocalDateTime createTime;

    }

}
