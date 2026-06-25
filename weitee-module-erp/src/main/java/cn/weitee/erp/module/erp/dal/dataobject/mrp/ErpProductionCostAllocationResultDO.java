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

@TableName("erp_production_cost_allocation_result")
@KeySequence("erp_production_cost_allocation_result_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProductionCostAllocationResultDO extends BaseDO {

    @TableId
    private Long id;

    private Long allocationId;

    private Long productionOrderId;

    private BigDecimal basisValue;

    private BigDecimal basisRatio;

    private BigDecimal allocatedAmount;

    private Long generatedCostEntryId;

    private String remark;

}
