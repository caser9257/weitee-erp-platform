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

@TableName("erp_finance_subject_balance")
@KeySequence("erp_finance_subject_balance_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceSubjectBalanceDO extends BaseDO {

    @TableId
    private Long id;

    private Long ledgerId;

    private Long periodId;

    private Integer periodSort;

    private String subjectCode;

    private String subjectName;

    private BigDecimal openingDebitAmount;

    private BigDecimal openingCreditAmount;

    private BigDecimal currentDebitAmount;

    private BigDecimal currentCreditAmount;

    private BigDecimal endingDebitAmount;

    private BigDecimal endingCreditAmount;

}
