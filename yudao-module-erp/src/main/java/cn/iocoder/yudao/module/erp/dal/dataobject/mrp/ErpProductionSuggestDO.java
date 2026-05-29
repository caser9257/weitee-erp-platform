package cn.iocoder.yudao.module.erp.dal.dataobject.mrp;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@TableName("erp_production_suggest")
@KeySequence("erp_production_suggest_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProductionSuggestDO extends BaseDO {

    @TableId
    private Long id;

    private Long planId;

    private Long traceNodeId;

    private Long projectId;

    private Long productId;

    private String tracePathKey;

    private Integer traceLevel;

    private Long parentMaterialId;

    private Long bomItemId;

    private BigDecimal suggestQty;

    private LocalDate suggestStartDate;

    private LocalDate suggestEndDate;

    private BigDecimal grossDemandQty;

    private BigDecimal availableStockQty;

    private BigDecimal incomingQty;

    private BigDecimal wipQty;

    private BigDecimal reservedStockQty;

    private BigDecimal safetyStockQty;

    private BigDecimal netDemandQty;

    private Long sourceOrderId;

    private Long sourceItemId;

    private Integer status;

    private Long convertProductionOrderId;

    private String remark;

}
