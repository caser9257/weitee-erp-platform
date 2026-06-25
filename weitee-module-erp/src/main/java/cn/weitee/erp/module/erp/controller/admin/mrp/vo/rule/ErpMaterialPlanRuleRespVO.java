package cn.weitee.erp.module.erp.controller.admin.mrp.vo.rule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 计划规则 Response VO")
@Data
public class ErpMaterialPlanRuleRespVO {

    private Long id;

    private Long productId;

    private String productName;

    private String supplyType;

    private String replenishMode;

    private BigDecimal safetyStock;

    private BigDecimal minOrderQty;

    private BigDecimal orderMultiple;

    private BigDecimal fixedOrderQty;

    private Integer purchaseLeadDay;

    private Integer makeLeadDay;

    private Boolean enableFlag;

    private Boolean shortageWarnFlag;

    private Long defaultSupplierId;

    private String defaultSupplierName;

    private String remark;

    private String validationStatus;

    private String validationMessage;

    private LocalDateTime createTime;

}
