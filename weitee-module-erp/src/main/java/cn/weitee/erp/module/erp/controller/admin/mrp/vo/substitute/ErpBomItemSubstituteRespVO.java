package cn.weitee.erp.module.erp.controller.admin.mrp.vo.substitute;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ErpBomItemSubstituteRespVO {

    private Long id;

    private Long bomId;

    private String bomCode;

    private Long bomItemId;

    private Long productId;

    private String productName;

    private Long materialId;

    private String materialName;

    private Long substituteMaterialId;

    private String substituteMaterialName;

    private Integer priority;

    private BigDecimal replaceRatio;

    private Boolean enableAutoRecommend;

    private Integer sort;

    private String remark;

    private LocalDateTime createTime;

}
