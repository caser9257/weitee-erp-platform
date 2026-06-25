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
import java.time.LocalDateTime;

@TableName("erp_finance_prepayment")
@KeySequence("erp_finance_prepayment_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinancePrepaymentDO extends BaseDO {

    @TableId
    private Long id;

    private String no;
    private Integer status;
    private LocalDateTime prepaymentTime;
    private Long financeUserId;
    private Long supplierId;
    private Long accountId;
    private BigDecimal prepaymentPrice;
    private BigDecimal allocatedPrice;
    private BigDecimal remainPrice;
    private String remark;

}
