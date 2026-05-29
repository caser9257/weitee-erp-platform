package cn.iocoder.yudao.module.erp.service.finance.diffcalc;

import java.math.BigDecimal;

/**
 * 双账套金额差异计算器接口
 * 根据不同的计算类型（按比例、固定差额、来源映射）计算外部账金额
 */
public interface AmountDiffCalculator {

    /**
     * 计算外部账金额
     *
     * @param internalAmount 内部账金额
     * @param ratio          比例系数（PRO_RATA 类型使用）
     * @param fixedAmount    固定差额（FIXED_VARIANCE 类型使用）
     * @return 外部账金额
     */
    BigDecimal calculate(BigDecimal internalAmount, BigDecimal ratio, BigDecimal fixedAmount);

    /**
     * 获取支持的计算类型
     *
     * @return 计算类型编号
     */
    Integer getCalculationType();

}
