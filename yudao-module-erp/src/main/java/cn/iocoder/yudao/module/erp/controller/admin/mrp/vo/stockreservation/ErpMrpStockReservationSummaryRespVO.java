package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.stockreservation;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ErpMrpStockReservationSummaryRespVO {

    private Long productId;

    private String productName;

    private BigDecimal stockQty;

    private BigDecimal activeReservedQty;

    private BigDecimal availableQty;

    private Integer activeProjectCount;

    private Integer activeReservationCount;

    private LocalDateTime lastReservedTime;

    private List<String> projectDistributionItems;

    private Integer projectDistributionMoreCount;

}
