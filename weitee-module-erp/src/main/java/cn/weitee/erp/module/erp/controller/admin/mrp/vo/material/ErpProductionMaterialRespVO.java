package cn.weitee.erp.module.erp.controller.admin.mrp.vo.material;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ErpProductionMaterialRespVO {

    private Long id;

    private Long productionOrderId;

    private Long materialId;

    private String materialName;

    private String materialCode;

    private String materialBarCode;

    private String unitName;

    private Long supplyWarehouseId;

    private String supplyWarehouseName;

    private BigDecimal requiredQty;

    private BigDecimal issuedQty;

    private BigDecimal returnedQty;

    private BigDecimal netIssuedQty;

    private BigDecimal remainingIssueQty;

    private Boolean batchControlFlag;

    private String remark;

}
