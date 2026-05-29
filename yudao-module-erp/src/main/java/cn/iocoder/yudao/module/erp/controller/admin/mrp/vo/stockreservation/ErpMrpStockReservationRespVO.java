package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.stockreservation;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ErpMrpStockReservationRespVO {

    private Long id;

    private Long planId;

    private String planNo;

    private Long projectId;

    private String projectNo;

    private String projectName;

    private Long productId;

    private String productName;

    private Long sourceOrderId;

    private String sourceOrderNo;

    private Long sourceItemId;

    private BigDecimal reservedQty;

    private Integer status;

    private LocalDateTime createTime;

}
