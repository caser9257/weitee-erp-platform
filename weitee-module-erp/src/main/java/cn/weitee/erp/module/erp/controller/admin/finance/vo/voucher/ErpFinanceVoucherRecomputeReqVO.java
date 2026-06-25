package cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 财务凭证重算 Request VO")
@Data
public class ErpFinanceVoucherRecomputeReqVO {

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "21")
    @NotNull(message = "业务类型不能为空")
    private Integer bizType;

    @Schema(description = "业务单据编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "业务单据编号不能为空")
    private Long bizId;

    @Schema(description = "重算说明", example = "模板配置修正后重算")
    private String remark;

}
