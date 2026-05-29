package cn.iocoder.yudao.module.erp.service.mrp.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpMrpNettingRequest {

    private Long materialId;

    private Long projectId;

    private BigDecimal grossDemandQty;

    private BigDecimal safetyStockQty;

}
