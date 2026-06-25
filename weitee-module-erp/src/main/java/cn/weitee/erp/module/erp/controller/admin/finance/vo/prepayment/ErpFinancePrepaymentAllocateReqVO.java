package cn.weitee.erp.module.erp.controller.admin.finance.vo.prepayment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - ERP 预付款核销 Request VO")
@Data
public class ErpFinancePrepaymentAllocateReqVO {

    @Schema(description = "预付款编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23752")
    @NotNull(message = "预付款编号不能为空")
    private Long prepaymentId;

    @Schema(description = "核销明细列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "核销明细列表不能为空")
    @Valid
    private List<Item> items;

    @Data
    public static class Item {

        @Schema(description = "应付台账编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11756")
        @NotNull(message = "应付台账编号不能为空")
        private Long apStatementId;

        @Schema(description = "核销金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "10000")
        @NotNull(message = "核销金额不能为空")
        @DecimalMin(value = "0", inclusive = false, message = "核销金额必须大于 0")
        private BigDecimal allocateAmount;

        @Schema(description = "备注", example = "预付核销")
        private String remark;

    }

}
