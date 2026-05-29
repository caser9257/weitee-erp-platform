package cn.iocoder.yudao.module.erp.service.mrp.support;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ErpBomPricingPreviewResult {

    private Long productId;
    private String productName;
    private Long bomId;
    private String bomCode;
    private String bomVersion;
    private BigDecimal materialUnitPrice;
    private String calcStatus;
    private String calcMessage;
    private List<Item> items = new ArrayList<>();
    private List<MissingMaterial> missingMaterials = new ArrayList<>();

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
