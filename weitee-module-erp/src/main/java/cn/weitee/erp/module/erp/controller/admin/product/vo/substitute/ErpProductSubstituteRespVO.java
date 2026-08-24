package cn.weitee.erp.module.erp.controller.admin.product.vo.substitute;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 物料替代料 Response VO")
@Data
public class ErpProductSubstituteRespVO {

    private Long id;

    private Long productId;

    private Long substituteProductId;

    private String substituteProductName;

    private String substituteMaterialCode;

    private Integer priority;

    private BigDecimal replaceRatio;

    private String remark;

}
