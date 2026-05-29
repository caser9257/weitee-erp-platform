package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.dualledger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 双账套重算 Request VO")
@Data
public class ErpFinanceDualLedgerRecomputeReqVO {

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "11")
    @NotNull(message = "业务类型不能为空")
    private Integer bizType;

    @Schema(description = "业务主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "88")
    @NotNull(message = "业务主键不能为空")
    private Long bizId;

    @Schema(description = "说明", example = "差异规则调整后重算")
    private String remark;
}
