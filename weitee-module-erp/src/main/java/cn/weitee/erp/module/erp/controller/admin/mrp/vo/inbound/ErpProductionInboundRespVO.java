package cn.weitee.erp.module.erp.controller.admin.mrp.vo.inbound;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ErpProductionInboundRespVO {

    private Long id;

    private String no;

    private Long finishQualityId;

    private String finishQualityNo;

    private Long productionOrderId;

    private String productionOrderNo;

    private Long projectId;

    private Long productId;

    private Long warehouseId;

    private String warehouseName;

    private BigDecimal inboundQty;

    private BigDecimal unitCost;

    private BigDecimal totalCost;

    private Integer status;

    private String statusName;

    private LocalDateTime inboundTime;

    private Long executedBy;

    private LocalDateTime executedTime;

    private String remark;

    private LocalDateTime createTime;

}
