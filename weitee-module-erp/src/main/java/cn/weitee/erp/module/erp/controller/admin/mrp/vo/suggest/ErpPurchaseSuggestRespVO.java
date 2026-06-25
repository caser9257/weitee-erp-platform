package cn.weitee.erp.module.erp.controller.admin.mrp.vo.suggest;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ErpPurchaseSuggestRespVO {

    private Long id;

    private Long planId;

    private Long traceNodeId;

    private Long projectId;

    private Long materialId;

    private String materialName;

    private String tracePathKey;

    private Integer traceLevel;

    private Long parentMaterialId;

    private Long bomItemId;

    private BigDecimal suggestQty;

    private LocalDate suggestArrivalDate;

    private BigDecimal grossDemandQty;

    private BigDecimal availableStockQty;

    private BigDecimal incomingQty;

    private BigDecimal wipQty;

    private BigDecimal reservedStockQty;

    private BigDecimal safetyStockQty;

    private BigDecimal netDemandQty;

    private Integer status;

    private Long sourceOrderId;

    private Long convertPurchaseOrderId;

    private LocalDateTime createTime;

}
