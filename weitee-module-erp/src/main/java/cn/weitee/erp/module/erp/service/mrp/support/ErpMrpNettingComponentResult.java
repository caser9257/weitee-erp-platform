package cn.weitee.erp.module.erp.service.mrp.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpMrpNettingComponentResult {

    private String componentCode;

    private String componentName;

    private String componentRole;

    private Integer sequenceNo;

    private Boolean enableFlag;

    private BigDecimal baseQty;

    private BigDecimal consumedQty;

    private BigDecimal remainingQty;

}
