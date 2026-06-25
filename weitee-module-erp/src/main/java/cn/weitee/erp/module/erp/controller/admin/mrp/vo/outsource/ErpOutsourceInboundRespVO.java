package cn.weitee.erp.module.erp.controller.admin.mrp.vo.outsource;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ErpOutsourceInboundRespVO {
    private Long id;
    private String inboundNo;
    private Long orderId;
    private String orderNo;
    private Long warehouseId;
    private String warehouseName;
    private String batchNo;
    private LocalDateTime inboundTime;
    private LocalDate produceDate;
    private LocalDate expireDate;
    private Integer status;
    private String statusName;
    private BigDecimal inboundQty;
    private BigDecimal materialCost;
    private BigDecimal processFee;
    private BigDecimal totalCost;
    private BigDecimal unitCost;
    private String remark;
    private String creatorName;
    private LocalDateTime createTime;

    /**
     * 关联台账信息
     */
    private Long statementId;
    private String statementNo;
    private BigDecimal statementAmount;
    private BigDecimal statementPaidAmount;
    private BigDecimal statementRemainAmount;
    private Integer statementStatus;
    private String statementStatusName;
}
