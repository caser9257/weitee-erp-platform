package cn.weitee.erp.module.erp.controller.admin.mrp.vo.outsource;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ErpOutsourceReturnRespVO {
    private Long id;
    private String returnNo;
    private Long orderId;
    private String orderNo;
    private LocalDateTime returnTime;
    private Integer status;
    private String statusName;
    private BigDecimal returnQty;
    private BigDecimal returnAmount;
    private String remark;
    private String creatorName;
    private LocalDateTime createTime;
    private List<Item> items;

    @Data
    public static class Item {
        private Long id;
        private Long materialId;
        private String materialName;
        private String materialCode;
        private String materialBarCode;
        private String productUnitName;
        private Long warehouseId;
        private String warehouseName;
        private BigDecimal returnQty;
        private BigDecimal returnAmount;
        private String remark;
        private List<Batch> batches;
    }

    @Data
    public static class Batch {
        private Long id;
        private Long issueBatchId;
        private Long stockBatchId;
        private String batchNo;
        private BigDecimal returnQty;
        private BigDecimal returnAmount;
    }
}
