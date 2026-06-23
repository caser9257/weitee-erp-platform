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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 应收台账 DO
 *
 * @author system
 */
@TableName("erp_ar_statement")
@KeySequence("erp_ar_statement_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ErpArStatementDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 台账编号
     */
    private String statementNo;

    /**
     * 业务类型：21=销售出库 22=销售退货
     */
    private Integer bizType;

    /**
     * 业务单据ID
     */
    private Long bizId;

    /**
     * 业务单据号
     */
    private String bizNo;

    /**
     * 来源销售订单ID
     */
    private Long sourceOrderId;

    /**
     * 来源销售订单号
     */
    private String sourceOrderNo;

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 结算账户ID
     */
    private Long accountId;

    /**
     * 应收金额
     */
    private BigDecimal amount;

    /**
     * 已收金额
     */
    private BigDecimal receivedAmount;

    /**
     * 剩余金额
     */
    private BigDecimal remainAmount;

    /**
     * 币种
     */
    private String currencyCode;

    /**
     * 业务日期
     */
    private LocalDate bizDate;

    /**
     * 到期日期
     */
    private LocalDate dueDate;

    /**
     * 开票状态：0=未开票 1=部分开票 2=已开票
     */
    private Integer invoiceStatus;

    /**
     * 发票号
     */
    private String invoiceNo;

    /**
     * 已开票金额
     */
    private BigDecimal invoiceAmount;

    /**
     * 状态：0=待收 1=部分收 2=已结清 3=已关闭
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

}
