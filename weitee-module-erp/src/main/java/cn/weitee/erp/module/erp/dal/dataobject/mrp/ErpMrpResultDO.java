package cn.weitee.erp.module.erp.dal.dataobject.mrp;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@TableName("erp_mrp_result")
@KeySequence("erp_mrp_result_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpMrpResultDO extends BaseDO {

    @TableId
    private Long id;

    private Long planId;

    private Long traceNodeId;

    private Long rootProductId;

    private Long materialId;

    private String tracePathKey;

    private Integer traceLevel;

    private Long parentMaterialId;

    private Long bomItemId;

    private BigDecimal grossDemandQty;

    private BigDecimal availableStockQty;

    private BigDecimal incomingQty;

    private BigDecimal wipQty;

    private BigDecimal reservedStockQty;

    private BigDecimal netDemandQty;

    private String policyCode;

    private Integer policyVersion;

    private String businessType;

    private Boolean mrpEnableFlag;

    private String supplyOwner;

    private String skipReason;

    private String suggestType;

    private LocalDate suggestDate;

    private Long sourceOrderId;

    private Long sourceItemId;

    private LocalDate demandDate;

}
