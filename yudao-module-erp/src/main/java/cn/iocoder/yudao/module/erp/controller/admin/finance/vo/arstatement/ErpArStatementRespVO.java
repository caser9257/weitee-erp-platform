package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.arstatement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 应收台账响应 VO
 *
 * @author system
 */
@Schema(description = "ERP - 应收台账响应 VO")
@Data
public class ErpArStatementRespVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "台账编号", example = "AR20240001")
    private String statementNo;

    @Schema(description = "业务类型：21=销售出库 22=销售退货", example = "21")
    private Integer bizType;

    @Schema(description = "业务单据ID", example = "1024")
    private Long bizId;

    @Schema(description = "业务单据号", example = "SO20240001")
    private String bizNo;

    @Schema(description = "来源销售订单ID", example = "1024")
    private Long sourceOrderId;

    @Schema(description = "来源销售订单号", example = "SO20240001")
    private String sourceOrderNo;

    @Schema(description = "客户ID", example = "1024")
    private Long customerId;

    @Schema(description = "客户名称", example = "示例客户")
    private String customerName;

    @Schema(description = "结算账户ID", example = "1024")
    private Long accountId;

    @Schema(description = "结算账户名称", example = "基本户")
    private String accountName;

    @Schema(description = "应收金额", example = "10000.00")
    private BigDecimal amount;

    @Schema(description = "已收金额", example = "5000.00")
    private BigDecimal receivedAmount;

    @Schema(description = "剩余金额", example = "5000.00")
    private BigDecimal remainAmount;

    @Schema(description = "币种", example = "CNY")
    private String currencyCode;

    @Schema(description = "业务日期", example = "2024-01-01")
    private LocalDate bizDate;

    @Schema(description = "到期日期", example = "2024-02-01")
    private LocalDate dueDate;

    @Schema(description = "开票状态：0=未开票 1=部分开票 2=已开票", example = "0")
    private Integer invoiceStatus;

    @Schema(description = "发票号")
    private String invoiceNo;

    @Schema(description = "已开票金额", example = "0")
    private BigDecimal invoiceAmount;

    @Schema(description = "状态：0=待收 1=部分收 2=已结清 3=已关闭", example = "0")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "台账明细列表")
    private List<Item> items;

    /**
     * 台账明细
     */
    @Data
    public static class Item {

        @Schema(description = "编号", example = "1024")
        private Long id;

        @Schema(description = "明细类型：1=应收 2=收款分配 3=收款退回", example = "1")
        private Integer itemType;

        @Schema(description = "关联类型：21=销售出库 22=销售退货 31=收款单", example = "21")
        private Integer refType;

        @Schema(description = "关联单据ID", example = "1024")
        private Long refId;

        @Schema(description = "关联单据号", example = "SO20240001")
        private String refNo;

        @Schema(description = "金额", example = "10000.00")
        private BigDecimal amount;

        @Schema(description = "操作后已收金额", example = "5000.00")
        private BigDecimal afterReceivedAmount;

        @Schema(description = "操作后剩余金额", example = "5000.00")
        private BigDecimal afterRemainAmount;

        @Schema(description = "备注")
        private String remark;

        @Schema(description = "创建时间")
        private LocalDateTime createTime;

    }

}
