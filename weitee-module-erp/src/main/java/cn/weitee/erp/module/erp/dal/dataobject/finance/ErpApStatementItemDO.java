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

@TableName("erp_ap_statement_item")
@KeySequence("erp_ap_statement_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpApStatementItemDO extends BaseDO {

    @TableId
    private Long id;

    private Long statementId;
    private Integer itemType;
    private Integer refType;
    private Long refId;
    private String refNo;
    private BigDecimal amount;
    private BigDecimal afterPaidAmount;
    private BigDecimal afterRemainAmount;
    private String remark;

}
