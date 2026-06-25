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
 * ERP 双账套差异项口径配置 DO
 */
@TableName("erp_finance_dual_ledger_diff_config")
@KeySequence("erp_finance_dual_ledger_diff_config_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ErpFinanceDualLedgerDiffConfigDO extends BaseDO {

    @TableId
    private Long id;

    private Integer bizType;

    private Integer diffItemType;

    private Integer externalSourceType;

    private Integer externalSourceValue;

    private Integer internalSourceType;

    private Integer internalSourceValue;

    /**
     * 计算类型：1-按比例分摊，2-固定差额，3-来源映射
     * @see cn.weitee.erp.module.erp.enums.ErpFinanceDiffCalculationTypeEnum
     */
    private Integer calculationType;

    /**
     * 比例系数（calculationType=1 时使用）
     * 外部账金额 = 内部账金额 × ratio
     */
    private BigDecimal ratio;

    /**
     * 固定差额（calculationType=2 时使用）
     * 外部账金额 = 内部账金额 - fixedAmount
     */
    private BigDecimal fixedAmount;

    private Integer status;

    private String remark;

}
