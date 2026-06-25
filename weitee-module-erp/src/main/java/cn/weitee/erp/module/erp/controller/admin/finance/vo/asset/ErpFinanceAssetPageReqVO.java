package cn.weitee.erp.module.erp.controller.admin.finance.vo.asset;

import cn.weitee.erp.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP 固定资产分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpFinanceAssetPageReqVO extends PageParam {

    @Schema(description = "资产编号", example = "GDZC20260520000001")
    private String no;

    @Schema(description = "资产名称", example = "研发电脑")
    private String name;

    @Schema(description = "资产分类", example = "电子设备")
    private String categoryName;

    @Schema(description = "资产类型", example = "0")
    private Integer assetType;

    @Schema(description = "状态", example = "10")
    private Integer status;
}
