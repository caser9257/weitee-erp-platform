package cn.weitee.erp.module.erp.controller.admin.mrp.vo.bom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - ERP BOM 计价预览 Response VO")
@Data
public class ErpBomPricingPreviewRespVO {

    @Schema(description = "产品编号")
    private Long productId;
    @Schema(description = "产品名称")
    private String productName;
    @Schema(description = "BOM 编号")
    private Long bomId;
    @Schema(description = "BOM 编号编码")
    private String bomCode;
    @Schema(description = "BOM 版本")
    private String bomVersion;
    @Schema(description = "单件物料成本")
    private BigDecimal materialUnitPrice;
    @Schema(description = "计算状态")
    private String calcStatus;
    @Schema(description = "计算说明")
    private String calcMessage;
    @Schema(description = "BOM 成本明细")
    private List<Item> items;
    @Schema(description = "缺失物料")
    private List<MissingMaterial> missingMaterials;

    @Data
    public static class Item {

        private Long materialId;
        private String materialName;
        private Long bomId;
        private String bomCode;
        private String bomVersion;
        private Integer level;
        private BigDecimal requiredQty;
        private BigDecimal unitPrice;
        private BigDecimal lineCost;
        private Integer materialType;
        private String supplyOwner;
        private String remark;
    }

    @Data
    public static class MissingMaterial {

        private Long materialId;
        private String materialName;
        private Long bomId;
        private Integer level;
        private BigDecimal requiredQty;
        private String reason;
    }

}
