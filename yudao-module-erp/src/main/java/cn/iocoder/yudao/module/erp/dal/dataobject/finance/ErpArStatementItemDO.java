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
 * 应收台账明细 DO
 *
 * @author system
 */
@TableName("erp_ar_statement_item")
@KeySequence("erp_ar_statement_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ErpArStatementItemDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 台账ID
     */
    private Long statementId;

    /**
     * 明细类型：1=应收 2=收款分配 3=收款退回
     */
    private Integer itemType;

    /**
     * 关联类型：21=销售出库 22=销售退货 31=收款单
     */
    private Integer refType;

    /**
     * 关联单据ID
     */
    private Long refId;

    /**
     * 关联单据号
     */
    private String refNo;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 操作后已收金额
     */
    private BigDecimal afterReceivedAmount;

    /**
     * 操作后剩余金额
     */
    private BigDecimal afterRemainAmount;

    /**
     * 备注
     */
    private String remark;

}
