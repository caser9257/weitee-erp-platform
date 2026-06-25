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

@TableName("erp_finance_prepayment_allocate")
@KeySequence("erp_finance_prepayment_allocate_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinancePrepaymentAllocateDO extends BaseDO {

    @TableId
    private Long id;

    private Long prepaymentId;
    private Long apStatementId;
    private BigDecimal allocateAmount;
    private Long supplierId;
    private Integer bizType;
    private Long bizId;
    private String bizNo;
    private Integer status;
    private String remark;

}
