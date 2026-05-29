package cn.iocoder.yudao.module.erp.service.mrp.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpMrpNettingResult {

    private String policyCode;

    private Integer policyVersion;

    private BigDecimal targetQty;

    private BigDecimal netDemandQty;

    private BigDecimal reservedStockQty;

    @Builder.Default
    private List<ErpMrpNettingComponentResult> componentResults = new ArrayList<>();

}
