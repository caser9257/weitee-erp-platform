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
 * ERP 双账套金额差异计算日志 DO
 */
@TableName("erp_finance_dual_ledger_amount_diff_log")
@KeySequence("erp_finance_dual_ledger_amount_diff_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceDualLedgerAmountDiffLogDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 源凭证编号（内部账）
     */
    private Long sourceVoucherId;

    /**
     * 目标凭证编号（外部账）
     */
    private Long targetVoucherId;

    /**
     * 差异项类型
     */
    private Integer diffItemType;

    /**
     * 计算类型：1-按比例分摊，2-固定差额，3-来源映射
     */
    private Integer calculationType;

    /**
     * 内部账金额
     */
    private BigDecimal internalAmount;

    /**
     * 外部账金额
     */
    private BigDecimal externalAmount;

    /**
     * 差异金额（内部 - 外部）
     */
    private BigDecimal diffAmount;

    /**
     * 比例系数
     */
    private BigDecimal ratio;

    /**
     * 固定差额
     */
    private BigDecimal fixedAmount;

    /**
     * 业务类型
     */
    private Integer bizType;

    /**
     * 业务单据编号
     */
    private Long bizId;

    /**
     * 备注
     */
    private String remark;

}
