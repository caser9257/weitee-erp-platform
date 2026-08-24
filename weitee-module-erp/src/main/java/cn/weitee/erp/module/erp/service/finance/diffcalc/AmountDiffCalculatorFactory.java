package cn.weitee.erp.module.erp.service.finance.diffcalc;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_DUAL_LEDGER_DIFF_CALCULATION_DIRECTION_INVALID;

import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 金额差异计算器工厂
 * 根据计算类型获取对应的计算器实现
 */
@Component
public class AmountDiffCalculatorFactory {

    private final Map<Integer, AmountDiffCalculator> calculatorMap = new HashMap<>();

    @Resource
    public void init(List<AmountDiffCalculator> calculators) {
        for (AmountDiffCalculator calculator : calculators) {
            calculatorMap.put(calculator.getCalculationType(), calculator);
        }
    }

    /**
     * 根据计算类型获取计算器
     *
     * @param calculationType 计算类型
     * @return 计算器实现，如果类型不支持则返回 null
     */
    public AmountDiffCalculator getCalculator(Integer calculationType) {
        return calculatorMap.get(calculationType);
    }

    /**
     * 计算外部账金额
     *
     * @param calculationType 计算类型
     * @param internalAmount  内部账金额
     * @param ratio           比例系数
     * @param fixedAmount     固定差额
     * @return 外部账金额
     */
    public BigDecimal calculate(Integer calculationType, BigDecimal internalAmount,
                                BigDecimal ratio, BigDecimal fixedAmount) {
        AmountDiffCalculator calculator = getCalculator(calculationType);
        if (calculator == null) {
            // 未找到计算器，返回原始金额
            return internalAmount;
        }
        return calculator.calculate(internalAmount, ratio, fixedAmount);
    }

    /**
     * 计算并校验外部账金额。所有差异成本重算都必须经过此方法，保证内账金额严格小于外账金额。
     */
    public BigDecimal calculateExternalAmount(Integer calculationType, BigDecimal internalAmount,
                                              BigDecimal ratio, BigDecimal fixedAmount) {
        BigDecimal externalAmount = calculate(calculationType, internalAmount, ratio, fixedAmount);
        if (internalAmount == null) {
            return null;
        }
        BigDecimal normalizedInternalAmount = internalAmount.setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal normalizedExternalAmount = externalAmount == null
                ? BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP)
                : externalAmount.setScale(2, java.math.RoundingMode.HALF_UP);
        if (normalizedInternalAmount.compareTo(BigDecimal.ZERO) != 0
                && normalizedExternalAmount.compareTo(normalizedInternalAmount) <= 0) {
            throw exception(FINANCE_DUAL_LEDGER_DIFF_CALCULATION_DIRECTION_INVALID,
                    normalizedInternalAmount, normalizedExternalAmount);
        }
        return normalizedExternalAmount;
    }

}
