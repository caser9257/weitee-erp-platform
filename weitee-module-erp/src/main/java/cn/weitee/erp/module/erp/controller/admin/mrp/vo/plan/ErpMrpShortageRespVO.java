package cn.weitee.erp.module.erp.controller.admin.mrp.vo.plan;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ErpMrpShortageRespVO {

    private Long id;

    private Long traceNodeId;

    private Long rootProductId;

    private String rootProductName;

    private Long materialId;

    private String materialName;

    private String tracePathKey;

    private Integer traceLevel;

    private Long parentMaterialId;

    private Long bomItemId;

    private BigDecimal shortageQty;

    private LocalDate requiredDate;

    private Long sourceOrderId;

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
