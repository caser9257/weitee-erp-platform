package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apestimate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "管理后台 - ERP 暂估扫描 Request VO")
@Data
public class ErpApEstimateScanReqVO {

    @Schema(description = "暂估月份", example = "2026-04")
    @NotBlank(message = "暂估月份不能为空")
    @Pattern(regexp = "\\d{4}-\\d{2}", message = "暂估月份格式必须为 yyyy-MM")
    private String estimateMonth;

}
