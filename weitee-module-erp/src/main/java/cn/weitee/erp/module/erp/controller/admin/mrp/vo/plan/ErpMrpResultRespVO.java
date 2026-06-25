package cn.weitee.erp.module.erp.controller.admin.mrp.vo.plan;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ErpMrpResultRespVO {

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

    private BigDecimal grossDemandQty;

    private BigDecimal availableStockQty;

    private BigDecimal incomingQty;

    private BigDecimal wipQty;

    private BigDecimal netDemandQty;

    private String policyCode;

    private Integer policyVersion;

    private String businessType;

    private Boolean mrpEnableFlag;

    private String supplyOwner;

    private String skipReason;

    private String suggestType;

    private LocalDate suggestDate;

    private Long sourceOrderId;

    private LocalDate demandDate;

}
