package cn.weitee.erp.module.erp.dal.dataobject.mrp;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@TableName("erp_mrp_trace_node")
@KeySequence("erp_mrp_trace_node_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpMrpTraceNodeDO extends BaseDO {

    @TableId
    private Long id;

    private Long planId;

    private Long rootProductId;

    private Long parentTraceNodeId;

    private Long parentMaterialId;

    private Integer traceLevel;

    private Long materialId;

    private Long bomId;

    private Long bomItemId;

    private String tracePathKey;

    private Long projectId;

    private Long sourceOrderId;

    private Long sourceItemId;

    private BigDecimal grossDemandQty;

    private BigDecimal availableStockQty;

    private BigDecimal incomingQty;

    private BigDecimal wipQty;

    private BigDecimal reservedStockQty;

    private BigDecimal safetyStockQty;

    private BigDecimal theoreticalNetDemandQty;

    private BigDecimal executionNetDemandQty;

    private String policyCode;

    private Integer policyVersion;

    private String businessType;

    private Boolean mrpEnableFlag;

    private String supplyOwner;

    private String suggestType;

    private String skipReason;

    private LocalDate suggestDate;

    private LocalDate demandDate;

}
