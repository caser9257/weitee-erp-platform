package cn.weitee.erp.module.erp.controller.admin.mrp.vo.outsource;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ErpOutsourceIssueRespVO {
    private Long id;
    private String issueNo;
    private Long orderId;
    private String orderNo;
    private Integer issueType;
    private String issueTypeName;
    private LocalDateTime issueTime;
    private Integer status;
    private String statusName;
    private BigDecimal issueQty;
    private BigDecimal issueAmount;
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
        private BigDecimal issueQty;
        private BigDecimal issueAmount;
        private String remark;
        private List<Batch> batches;
    }

    @Data
    public static class Batch {
        private Long id;
        private Long stockBatchId;
        private String batchNo;
        private BigDecimal issueQty;
        private BigDecimal issueAmount;
        private LocalDateTime inboundTime;
        private LocalDate produceDate;
        private LocalDate expireDate;
    }
}
