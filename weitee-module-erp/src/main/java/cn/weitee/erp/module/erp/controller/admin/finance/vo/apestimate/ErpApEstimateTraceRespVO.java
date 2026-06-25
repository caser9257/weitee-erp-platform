package cn.weitee.erp.module.erp.controller.admin.finance.vo.apestimate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 暂估闭环追溯 Response VO")
@Data
public class ErpApEstimateTraceRespVO {

    @Schema(description = "暂估单信息")
    private ErpApEstimateRespVO estimate;

    @Schema(description = "来源应付台账信息")
    private Statement statement;

    @Data
    public static class Statement {

        @Schema(description = "台账编号", example = "31")
        private Long id;

        @Schema(description = "台账号", example = "AP-11-PI-001")
        private String statementNo;

        @Schema(description = "业务类型", example = "11")
        private Integer bizType;

        @Schema(description = "业务单号", example = "PI-001")
        private String bizNo;

        @Schema(description = "来源订单编号", example = "21")
        private Long sourceOrderId;

        @Schema(description = "来源订单号", example = "PO-001")
        private String sourceOrderNo;

        @Schema(description = "业务日期")
        private LocalDateTime bizDate;

        @Schema(description = "到期日期")
        private LocalDateTime dueDate;

        @Schema(description = "台账金额", example = "95.00")
        private BigDecimal amount;

        @Schema(description = "已付款金额", example = "0")
        private BigDecimal paidAmount;

        @Schema(description = "未付款金额", example = "95.00")
        private BigDecimal remainAmount;

        @Schema(description = "台账状态", example = "10")
        private Integer status;

        @Schema(description = "台账状态名称", example = "未付款")
        private String statusName;

        @Schema(description = "收票状态", example = "2")
        private Integer invoiceStatus;

        @Schema(description = "收票状态名称", example = "已收票")
        private String invoiceStatusName;

        @Schema(description = "发票号摘要", example = "INV-20260428-001")
        private String invoiceNo;

        @Schema(description = "发票金额摘要", example = "95.00")
        private BigDecimal invoiceAmount;

        @Schema(description = "备注", example = "采购入库生成")
        private String remark;

    }

}
