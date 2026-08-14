package cn.weitee.erp.module.erp.controller.admin.mrp.vo.bom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP BOM Response VO")
@Data
public class ErpBomRespVO {

    private Long id;

    private String bomCode;

    private Long productId;

    private Long routeId;

    private String productName;

    private String version;

    private BigDecimal yieldRate;

    private Integer status;

    private LocalDate effectiveDate;

    private LocalDate expireDate;

    private Long sourceRdBomId;

    private String remark;

    private LocalDateTime createTime;

    private List<Item> items;

    @Data
    public static class Item {

        private Long id;

        private Long materialId;

        private String materialName;

        private Integer materialType;

        private Long unitId;

        private String unitName;

        private BigDecimal usageQty;

        private BigDecimal lossRate;

        private String referenceDesignator;

        private Integer issueMode;

        private Boolean backflushFlag;

        private Integer leadTimeDay;

        private Long supplyWarehouseId;

        private Long requiredStepId;

        private Boolean mrpEnableFlag;

        private String supplyOwner;

        private Integer sort;

        private String remark;

        private Integer level;

        private Long childBomId;

        private String childBomCode;

        private String childBomVersion;

        private Boolean hasChildrenBom;

        private List<Item> children;

        private List<Substitute> substitutes;

        @Data
        public static class Substitute {

            private Long id;

            private Long substituteMaterialId;

            private String substituteMaterialName;

            private Integer priority;

            private BigDecimal replaceRatio;

            private Boolean enableAutoRecommend;

            private Integer sort;

            private String remark;

        }

    }

}
