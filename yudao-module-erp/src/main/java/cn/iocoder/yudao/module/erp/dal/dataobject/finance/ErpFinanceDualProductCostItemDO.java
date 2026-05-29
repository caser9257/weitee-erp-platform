package cn.iocoder.yudao.module.erp.dal.dataobject.finance;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * ERP 产品双账成本明细 DO
 */
@TableName("erp_finance_dual_product_cost_item")
@KeySequence("erp_finance_dual_product_cost_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceDualProductCostItemDO extends BaseDO {

    @TableId
    private Long id;
    private Long resultId;
    private Integer costComponentType;
    private Integer sourceType;
    private Integer sourceBizType;
    private Long sourceBizId;
    private String sourceNo;
    private Long productId;
    private Long productionOrderId;
    private String period;
    private BigDecimal externalAmount;
    private BigDecimal internalAmount;
    private BigDecimal diffAmount;
}
