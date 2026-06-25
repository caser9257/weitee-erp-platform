package cn.weitee.erp.module.erp.controller.admin.mrp.vo.quality;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ErpProductionFinishQualityRespVO {

    private Long id;

    private String no;

    private Long productionOrderId;

    private String productionOrderNo;

    private Long sourceOrderId;

    private Long sourceItemId;

    private Long productId;

    private BigDecimal reportQty;

    private BigDecimal qualifiedQty;

    private BigDecimal unqualifiedQty;

    private Integer status;

    private Long checkerUserId;

    private LocalDateTime checkTime;

    private String remark;

    private LocalDateTime createTime;

}
