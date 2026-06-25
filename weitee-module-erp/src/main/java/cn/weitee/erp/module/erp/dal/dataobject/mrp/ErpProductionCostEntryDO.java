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

@TableName("erp_production_cost_entry")
@KeySequence("erp_production_cost_entry_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProductionCostEntryDO extends BaseDO {

    @TableId
    private Long id;

    private Long productionOrderId;

    private Integer costType;

    private Integer sourceType;

    private String accountingMonth;

    private BigDecimal amount;

    private String remark;
    /**
     * 来源单据编号（如盘点单ID）
     */
    private Long sourceId;
    /**
     * 来源单据号（如盘点单号）
     */
    private String sourceNo;

}
