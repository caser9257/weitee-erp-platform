package cn.weitee.erp.module.erp.controller.admin.finance.vo.asset;

import cn.weitee.erp.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP 固定资产折旧分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpFinanceAssetDepreciationPageReqVO extends PageParam {

    @Schema(description = "资产编号", example = "1")
    private Long assetId;

    @Schema(description = "折旧期间", example = "2026-05")
    private String period;
}
