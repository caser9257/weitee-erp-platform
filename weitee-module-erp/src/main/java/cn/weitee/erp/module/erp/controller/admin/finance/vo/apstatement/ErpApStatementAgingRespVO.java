package cn.weitee.erp.module.erp.controller.admin.finance.vo.apstatement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 应付台账账龄 Response VO")
@Data
public class ErpApStatementAgingRespVO {

    @Schema(description = "供应商编号", example = "1")
    private Long supplierId;

    @Schema(description = "供应商名称", example = "华东供应商")
    private String supplierName;

    @Schema(description = "0-30 天金额", example = "100.00")
    private BigDecimal amount0To30;

    @Schema(description = "31-60 天金额", example = "0.00")
    private BigDecimal amount31To60;

    @Schema(description = "61-90 天金额", example = "0.00")
    private BigDecimal amount61To90;

    @Schema(description = "91 天以上金额", example = "0.00")
    private BigDecimal amount91Plus;

    @Schema(description = "剩余合计", example = "100.00")
    private BigDecimal totalRemainAmount;

}
