package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.outsource;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ErpOutsourceLossEntryRespVO {

    private Long id;
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
    private BigDecimal lossQty;
    private BigDecimal lossAmount;
    private BigDecimal unitPrice;
    private String remark;
    private LocalDateTime createTime;

}
