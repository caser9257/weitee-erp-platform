package cn.weitee.erp.module.erp.controller.admin.mrp.vo.stockreservation;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ErpMrpStockReservationProjectSummaryRespVO {

    private Long productId;

    private Long projectId;

    private String projectNo;

    private String projectName;

    private BigDecimal activeReservedQty;

    private Integer activeReservationCount;

    private Integer sourceOrderCount;

    private LocalDateTime lastReservedTime;

}
