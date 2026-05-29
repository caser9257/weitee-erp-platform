package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 应付台账汇总 Response VO")
@Data
public class ErpApStatementSummaryRespVO {

    @Schema(description = "供应商编号", example = "1")
    private Long supplierId;

    @Schema(description = "供应商名称", example = "华东供应商")
    private String supplierName;

    @Schema(description = "台账数", example = "3")
    private Long statementCount;

    @Schema(description = "应付合计", example = "300.00")
    private BigDecimal totalAmount;

    @Schema(description = "已核销合计", example = "200.00")
    private BigDecimal totalPaidAmount;

    @Schema(description = "剩余合计", example = "100.00")
    private BigDecimal totalRemainAmount;

}
