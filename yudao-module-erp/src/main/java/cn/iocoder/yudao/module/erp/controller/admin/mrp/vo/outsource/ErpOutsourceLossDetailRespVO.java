package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.outsource;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ErpOutsourceLossDetailRespVO {

    private Long orderId;
    private String orderNo;
    private BigDecimal plannedQty;
    private BigDecimal finishedQty;
    private BigDecimal orderLossQty;
    private BigDecimal recordedLossQty;
    private BigDecimal pendingBackfillQty;
    private BigDecimal unresolvedQty;
    private Boolean hasPendingBackfill;
    private String closeRemark;
    private List<ErpOutsourceLossDetailItemRespVO> details;
    private List<ErpOutsourceLossEntryRespVO> entries;

}
