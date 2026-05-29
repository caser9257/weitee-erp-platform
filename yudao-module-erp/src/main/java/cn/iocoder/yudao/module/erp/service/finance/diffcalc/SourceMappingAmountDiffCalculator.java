package cn.iocoder.yudao.module.erp.service.finance.diffcalc;

import cn.iocoder.yudao.module.erp.enums.ErpFinanceDiffCalculationTypeEnum;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 来源映射金额差异计算器
 * 外部账和内部账从不同的成本来源取值，金额由来源数据决定
 * 此计算器不做金额转换，返回原始金额（实际金额由调用方根据来源配置查询）
 */
@Component
public class SourceMappingAmountDiffCalculator implements AmountDiffCalculator {

    @Override
    public BigDecimal calculate(BigDecimal internalAmount, BigDecimal ratio, BigDecimal fixedAmount) {
        // 来源映射模式下，金额由调用方根据 externalSourceType/Value 查询成本数据得出
        // 此处返回原始金额，实际差异在调用方处理
        return internalAmount;
    }

    @Override
    public Integer getCalculationType() {
        return ErpFinanceDiffCalculationTypeEnum.SOURCE_MAPPING.getType();
    }

}
