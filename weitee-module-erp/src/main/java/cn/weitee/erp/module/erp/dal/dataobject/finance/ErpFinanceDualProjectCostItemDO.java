package cn.weitee.erp.module.erp.dal.dataobject.finance;

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
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * ERP 项目双账成本明细 DO
 */
@TableName("erp_finance_dual_project_cost_item")
@KeySequence("erp_finance_dual_project_cost_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceDualProjectCostItemDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 结果ID
     */
    private Long resultId;

    /**
     * 来源类型（10-费用报销, 20-研发费用, 30-租赁折旧, 40-其他）
     */
    private Integer sourceType;

    /**
     * 来源业务类型
     */
    private Integer sourceBizType;

    /**
     * 来源业务ID
     */
    private Long sourceBizId;

    /**
     * 来源单号
     */
    private String sourceNo;

    /**
     * 成本类别
     */
    private Integer costType;

    /**
     * 外部账金额
     */
    private BigDecimal externalAmount;

    /**
     * 内部账金额
     */
    private BigDecimal internalAmount;

    /**
     * 差异金额
     */
    private BigDecimal diffAmount;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 期间
     */
    private String period;
}
