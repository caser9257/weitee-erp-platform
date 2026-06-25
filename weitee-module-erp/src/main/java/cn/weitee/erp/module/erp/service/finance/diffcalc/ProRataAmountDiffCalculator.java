package cn.weitee.erp.module.erp.service.finance.diffcalc;

import cn.weitee.erp.module.erp.enums.ErpFinanceDiffCalculationTypeEnum;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 按比例分摊金额差异计算器
 * 公式：外部账金额 = 内部账金额 × 比例系数
 */
@Component
public class ProRataAmountDiffCalculator implements AmountDiffCalculator {

    @Override
    public BigDecimal calculate(BigDecimal internalAmount, BigDecimal ratio, BigDecimal fixedAmount) {
        if (internalAmount == null || ratio == null) {
            return internalAmount;
        }
        return internalAmount.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public Integer getCalculationType() {
        return ErpFinanceDiffCalculationTypeEnum.PRO_RATA.getType();
    }

}
