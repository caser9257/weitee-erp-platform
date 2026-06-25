package cn.weitee.erp.module.erp.controller.admin.finance.vo.period;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 会计期间新增 Request VO")
@Data
public class ErpFinancePeriodSaveReqVO {

    @Schema(description = "账簿编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "账簿编号不能为空")
    private Long ledgerId;

    @Schema(description = "会计年度", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026")
    @NotNull(message = "会计年度不能为空")
    @Min(value = 2000, message = "会计年度不能早于 2000")
    @Max(value = 2099, message = "会计年度不能晚于 2099")
    private Integer periodYear;

    @Schema(description = "会计月份", requiredMode = Schema.RequiredMode.REQUIRED, example = "4")
    @NotNull(message = "会计月份不能为空")
    @Min(value = 1, message = "会计月份最小为 1")
    @Max(value = 12, message = "会计月份最大为 12")
    private Integer periodMonth;

    @Schema(description = "备注", example = "手工补建")
    private String remark;
}
