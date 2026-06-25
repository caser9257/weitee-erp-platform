package cn.weitee.erp.module.erp.controller.admin.mrp.vo.outsource;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ErpOutsourceFeeRespVO {
    private Long id;
    private String feeNo;
    private Long orderId;
    private String orderNo;
    private Long statementId;
    private String statementNo;
    private LocalDateTime feeTime;
    private Integer status;
    private String statusName;
    private BigDecimal feeAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainAmount;
    private Integer reconciliationStatus;
    private String reconciliationStatusName;
    private String remark;
    private String creatorName;
    private LocalDateTime createTime;
}
