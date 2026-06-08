package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "管理后台 - ERP 固定资产折旧生成 Request VO")
@Data
public class ErpFinanceAssetDepreciationGenerateReqVO {

    @NotBlank(message = "折旧期间不能为空")
    private String period;
}
