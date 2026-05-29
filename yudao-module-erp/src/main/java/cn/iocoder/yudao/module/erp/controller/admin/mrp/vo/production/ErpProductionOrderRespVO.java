package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.production;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ErpProductionOrderRespVO {

    private Long id;

    private String orderNo;

    private Long productId;

    private String productName;

    private BigDecimal planQty;

    private BigDecimal finishedQty;

    private LocalDateTime planStartTime;

    private LocalDateTime planEndTime;

    private Integer status;

    private String sourceType;

    private Long sourceId;

    private Long sourceOrderId;

    private Long sourceItemId;

    private String remark;

    private LocalDateTime createTime;

}
