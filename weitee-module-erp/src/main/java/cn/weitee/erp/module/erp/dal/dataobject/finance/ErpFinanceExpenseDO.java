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
import java.time.LocalDateTime;

/**
 * ERP 零星报销单 DO
 */
@TableName("erp_finance_expense")
@KeySequence("erp_finance_expense_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceExpenseDO extends BaseDO {

    @TableId
    private Long id;

    private String no;
    private Integer status;
    private String processInstanceId;
    private LocalDateTime expenseTime;
    private Integer expenseType;
    /**
     * 研发支出分类（费用化/资本化）
     */
    private Integer researchCategory;
    /**
     * 研发支出口径（10-费用化，20-资本化）
     */
    private Integer rdAccountingType;
    private Long deptId;
    private Long projectId;
    private Long supplierId;
    private Long financeUserId;
    private Long accountId;
    private BigDecimal expensePrice;
    private BigDecimal paidPrice;
    private BigDecimal remainPrice;
    private String remark;
    /**
     * 租赁合同编号
     */
    private String leaseContractNo;

}
