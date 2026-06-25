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

import java.math.BigDecimal;

/**
 * ERP 费用报销单明细 DO
 */
@TableName("erp_finance_expense_item")
@KeySequence("erp_finance_expense_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceExpenseItemDO extends BaseDO {

    @TableId
    private Long id;

    private Long expenseId;

    private String itemName;

    private BigDecimal amount;

    private String remark;

    private Boolean assetCandidateFlag;

}
