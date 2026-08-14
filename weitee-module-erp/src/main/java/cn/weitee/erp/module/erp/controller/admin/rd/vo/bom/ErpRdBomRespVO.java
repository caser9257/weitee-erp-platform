package cn.weitee.erp.module.erp.controller.admin.rd.vo.bom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 研发 BOM Response VO")
@Data
public class ErpRdBomRespVO {

    private Long id;

    private String bomCode;

    private Long productId;

    private String productName;

    private String version;

    private Integer status;

    private Long publishedBomId;

    private LocalDateTime lastPublishedTime;

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

        private Integer leadTimeDay;

        private Integer sort;

        private String remark;

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
