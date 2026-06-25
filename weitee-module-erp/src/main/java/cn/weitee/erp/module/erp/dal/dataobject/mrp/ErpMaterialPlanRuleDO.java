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

@TableName("erp_material_plan_rule")
@KeySequence("erp_material_plan_rule_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpMaterialPlanRuleDO extends BaseDO {

    @TableId
    private Long id;

    private Long productId;

    private String supplyType;

    private String replenishMode;

    private BigDecimal safetyStock;

    private BigDecimal minOrderQty;

    private BigDecimal orderMultiple;

    private BigDecimal fixedOrderQty;

    private Integer purchaseLeadDay;

    private Integer makeLeadDay;

    private Boolean enableFlag;

    private Boolean shortageWarnFlag;

    private Long defaultSupplierId;

    private String remark;

}
