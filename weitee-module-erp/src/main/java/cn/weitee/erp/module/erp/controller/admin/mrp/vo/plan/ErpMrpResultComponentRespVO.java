package cn.weitee.erp.module.erp.controller.admin.mrp.vo.plan;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ErpMrpResultComponentRespVO {

    private String componentCode;

    private String componentName;

    private String componentRole;

    private Integer sequenceNo;

    private Boolean enableFlag;

    private BigDecimal baseQty;

    private BigDecimal consumedQty;

    private BigDecimal remainingQty;
}
