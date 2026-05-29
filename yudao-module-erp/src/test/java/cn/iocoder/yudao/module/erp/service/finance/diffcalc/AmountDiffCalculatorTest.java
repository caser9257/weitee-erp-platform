package cn.iocoder.yudao.module.erp.service.finance.diffcalc;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 金额差异计算器单元测试
 */
class AmountDiffCalculatorTest {

    @Test
    void proRata_calculate_shouldMultiplyByRatio() {
        ProRataAmountDiffCalculator calculator = new ProRataAmountDiffCalculator();

        // 100 * 0.85 = 85
        BigDecimal result = calculator.calculate(
                new BigDecimal("100.00"),
                new BigDecimal("0.85"),
                null);

        assertEquals(new BigDecimal("85.00"), result);
    }

    @Test
    void proRata_calculate_shouldHandleNullRatio() {
        ProRataAmountDiffCalculator calculator = new ProRataAmountDiffCalculator();

        BigDecimal result = calculator.calculate(
                new BigDecimal("100.00"),
                null,
                null);

        assertEquals(new BigDecimal("100.00"), result);
    }

    @Test
    void proRata_calculate_shouldHandleNullAmount() {
        ProRataAmountDiffCalculator calculator = new ProRataAmountDiffCalculator();

        BigDecimal result = calculator.calculate(
                null,
                new BigDecimal("0.85"),
                null);

        assertNull(result);
    }

    @Test
    void fixedVariance_calculate_shouldSubtractFixedAmount() {
        FixedVarianceAmountDiffCalculator calculator = new FixedVarianceAmountDiffCalculator();

        // 100 - 20 = 80
        BigDecimal result = calculator.calculate(
                new BigDecimal("100.00"),
                null,
                new BigDecimal("20.00"));

        assertEquals(new BigDecimal("80.00"), result);
    }

    @Test
    void fixedVariance_calculate_shouldHandleNullFixedAmount() {
        FixedVarianceAmountDiffCalculator calculator = new FixedVarianceAmountDiffCalculator();

        BigDecimal result = calculator.calculate(
                new BigDecimal("100.00"),
                null,
                null);

        assertEquals(new BigDecimal("100.00"), result);
    }

    @Test
    void fixedVariance_calculate_shouldHandleNullAmount() {
        FixedVarianceAmountDiffCalculator calculator = new FixedVarianceAmountDiffCalculator();

        BigDecimal result = calculator.calculate(
                null,
                null,
                new BigDecimal("20.00"));

        assertNull(result);
    }

    @Test
    void sourceMapping_calculate_shouldReturnOriginalAmount() {
        SourceMappingAmountDiffCalculator calculator = new SourceMappingAmountDiffCalculator();

        BigDecimal result = calculator.calculate(
                new BigDecimal("100.00"),
                null,
                null);

        assertEquals(new BigDecimal("100.00"), result);
    }

    @Test
    void proRata_shouldReturnCorrectType() {
        ProRataAmountDiffCalculator calculator = new ProRataAmountDiffCalculator();
        assertEquals(1, calculator.getCalculationType());
    }

    @Test
    void fixedVariance_shouldReturnCorrectType() {
        FixedVarianceAmountDiffCalculator calculator = new FixedVarianceAmountDiffCalculator();
        assertEquals(2, calculator.getCalculationType());
    }

    @Test
    void sourceMapping_shouldReturnCorrectType() {
        SourceMappingAmountDiffCalculator calculator = new SourceMappingAmountDiffCalculator();
        assertEquals(3, calculator.getCalculationType());
    }

    @Test
    void factory_shouldReturnCorrectCalculator() {
        AmountDiffCalculatorFactory factory = new AmountDiffCalculatorFactory();
        factory.init(java.util.Arrays.asList(
                new ProRataAmountDiffCalculator(),
                new FixedVarianceAmountDiffCalculator(),
                new SourceMappingAmountDiffCalculator()
        ));

        assertNotNull(factory.getCalculator(1));
        assertNotNull(factory.getCalculator(2));
        assertNotNull(factory.getCalculator(3));
        assertNull(factory.getCalculator(99));
    }

    @Test
    void factory_calculate_shouldUseCorrectStrategy() {
        AmountDiffCalculatorFactory factory = new AmountDiffCalculatorFactory();
        factory.init(java.util.Arrays.asList(
                new ProRataAmountDiffCalculator(),
                new FixedVarianceAmountDiffCalculator(),
                new SourceMappingAmountDiffCalculator()
        ));

        // PRO_RATA: 100 * 0.85 = 85
        assertEquals(new BigDecimal("85.00"),
                factory.calculate(1, new BigDecimal("100.00"), new BigDecimal("0.85"), null));

        // FIXED_VARIANCE: 100 - 20 = 80
        assertEquals(new BigDecimal("80.00"),
                factory.calculate(2, new BigDecimal("100.00"), null, new BigDecimal("20.00")));

        // SOURCE_MAPPING: returns original
        assertEquals(new BigDecimal("100.00"),
                factory.calculate(3, new BigDecimal("100.00"), null, null));
    }

}
