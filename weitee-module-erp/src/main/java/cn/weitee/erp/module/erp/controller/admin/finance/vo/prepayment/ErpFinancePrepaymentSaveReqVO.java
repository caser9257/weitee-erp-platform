package cn.weitee.erp.module.erp.controller.admin.finance.vo.prepayment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 预付款新增/修改 Request VO")
@Data
public class ErpFinancePrepaymentSaveReqVO {

    @Schema(description = "编号", example = "23752")
    private Long id;

    @Schema(description = "预付款时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "预付款时间不能为空")
    private LocalDateTime prepaymentTime;

    @Schema(description = "财务人员编号", example = "19690")
    private Long financeUserId;

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "29399")
    @NotNull(message = "供应商编号不能为空")
    private Long supplierId;

    @Schema(description = "结算账户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "28989")
    @NotNull(message = "结算账户编号不能为空")
    private Long accountId;

    @Schema(description = "预付金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "10000")
    @NotNull(message = "预付金额不能为空")
    private BigDecimal prepaymentPrice;

    @Schema(description = "备注", example = "预付")
    private String remark;

}
