package cn.weitee.erp.module.erp.service.finance.diffcalc;

import cn.weitee.erp.module.erp.enums.ErpFinanceDiffCalculationTypeEnum;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 固定差额金额差异计算器
 * 公式：外部账金额 = 内部账金额 - 固定差额
 */
@Component
public class FixedVarianceAmountDiffCalculator implements AmountDiffCalculator {

    @Override
    public BigDecimal calculate(BigDecimal internalAmount, BigDecimal ratio, BigDecimal fixedAmount) {
        if (internalAmount == null || fixedAmount == null) {
            return internalAmount;
        }
        return internalAmount.subtract(fixedAmount).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public Integer getCalculationType() {
        return ErpFinanceDiffCalculationTypeEnum.FIXED_VARIANCE.getType();
    }

}
