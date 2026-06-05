package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.rdmonthend;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "管理后台 - 研发月末处理 Request VO")
@Data
public class ErpFinanceRdMonthEndExecuteReqVO {

    @Schema(description = "期间，格式 yyyy-MM", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-05")
    @NotBlank(message = "期间不能为空")
    private String period;
}
