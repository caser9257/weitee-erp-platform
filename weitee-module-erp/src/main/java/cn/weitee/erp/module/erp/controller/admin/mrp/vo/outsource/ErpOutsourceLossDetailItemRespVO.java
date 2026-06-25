package cn.weitee.erp.module.erp.controller.admin.mrp.vo.outsource;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ErpOutsourceLossDetailItemRespVO {

    private Long issueBatchId;
    private Long materialId;
    private String materialName;
    private String materialCode;
    private String materialBarCode;
    private String productUnitName;
    private Long warehouseId;
    private String warehouseName;
    private Long stockBatchId;
    private String batchNo;
    private BigDecimal issueQty;
    private BigDecimal returnedQty;
    private BigDecimal lossQty;
    private BigDecimal availableLossQty;
    private BigDecimal issueAmount;
    private BigDecimal returnedAmount;
    private BigDecimal lossAmount;
    private BigDecimal unitPrice;
    private String remark;

}
