package cn.iocoder.yudao.module.erp.enums;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

class ErpFinanceVoucherAmountSourceEnumTest {

    @Test
    void testEnumValues() {
        // 验证所有枚举值存在
        assertNotNull(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT);
        assertNotNull(ErpFinanceVoucherAmountSourceEnum.FIXED_AMOUNT);
        assertNotNull(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT_RATE);
        assertNotNull(ErpFinanceVoucherAmountSourceEnum.RESEARCH_EXPENSE_CATEGORY);
        assertNotNull(ErpFinanceVoucherAmountSourceEnum.RESEARCH_PROJECT_SUMMARY);
    }

    @Test
    void testEnumTypes() {
        // 验证枚举类型值
        assertEquals(10, ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.getType());
        assertEquals(20, ErpFinanceVoucherAmountSourceEnum.FIXED_AMOUNT.getType());
        assertEquals(30, ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT_RATE.getType());
        assertEquals(40, ErpFinanceVoucherAmountSourceEnum.RESEARCH_EXPENSE_CATEGORY.getType());
        assertEquals(50, ErpFinanceVoucherAmountSourceEnum.RESEARCH_PROJECT_SUMMARY.getType());
    }

    @Test
    void testEnumNames() {
        // 验证枚举名称
        assertEquals("业务金额", ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.getName());
        assertEquals("固定金额", ErpFinanceVoucherAmountSourceEnum.FIXED_AMOUNT.getName());
        assertEquals("业务金额比例", ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT_RATE.getName());
        assertEquals("研发支出分类", ErpFinanceVoucherAmountSourceEnum.RESEARCH_EXPENSE_CATEGORY.getName());
        assertEquals("研发项目汇总", ErpFinanceVoucherAmountSourceEnum.RESEARCH_PROJECT_SUMMARY.getName());
    }

    @Test
    void testNeedValue() {
        // 验证needValue属性
        assertFalse(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.isNeedValue());
        assertTrue(ErpFinanceVoucherAmountSourceEnum.FIXED_AMOUNT.isNeedValue());
        assertTrue(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT_RATE.isNeedValue());
        assertFalse(ErpFinanceVoucherAmountSourceEnum.RESEARCH_EXPENSE_CATEGORY.isNeedValue());
        assertFalse(ErpFinanceVoucherAmountSourceEnum.RESEARCH_PROJECT_SUMMARY.isNeedValue());
    }

    @Test
    void testFromArray() {
        // 验证ARRAYS数组包含所有类型
        Integer[] arrays = ErpFinanceVoucherAmountSourceEnum.ARRAYS;
        assertNotNull(arrays);
        assertEquals(5, arrays.length);
        assertTrue(java.util.Arrays.asList(arrays).contains(10));
        assertTrue(java.util.Arrays.asList(arrays).contains(20));
        assertTrue(java.util.Arrays.asList(arrays).contains(30));
        assertTrue(java.util.Arrays.asList(arrays).contains(40));
        assertTrue(java.util.Arrays.asList(arrays).contains(50));
    }

    @Test
    void testFromType() {
        // 验证fromType方法
        assertEquals(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT,
                ErpFinanceVoucherAmountSourceEnum.fromType(10));
        assertEquals(ErpFinanceVoucherAmountSourceEnum.FIXED_AMOUNT,
                ErpFinanceVoucherAmountSourceEnum.fromType(20));
        assertEquals(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT_RATE,
                ErpFinanceVoucherAmountSourceEnum.fromType(30));
        assertEquals(ErpFinanceVoucherAmountSourceEnum.RESEARCH_EXPENSE_CATEGORY,
                ErpFinanceVoucherAmountSourceEnum.fromType(40));
        assertEquals(ErpFinanceVoucherAmountSourceEnum.RESEARCH_PROJECT_SUMMARY,
                ErpFinanceVoucherAmountSourceEnum.fromType(50));
        assertNull(ErpFinanceVoucherAmountSourceEnum.fromType(99));
    }

    @Test
    void testResolveAmount_BizAmount() {
        // 测试业务金额解析
        BigDecimal bizAmount = new BigDecimal("1000.50");
        BigDecimal result = ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.resolveAmount(null, bizAmount);
        assertEquals(bizAmount, result);
    }

    @Test
    void testResolveAmount_BizAmount_Null() {
        // 测试业务金额解析 - null值
        BigDecimal result = ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.resolveAmount(null, null);
        assertNull(result);
    }

    @Test
    void testResolveAmount_FixedAmount() {
        // 测试固定金额解析
        BigDecimal sourceValue = new BigDecimal("500.75");
        BigDecimal result = ErpFinanceVoucherAmountSourceEnum.FIXED_AMOUNT.resolveAmount(sourceValue, null);
        assertEquals(new BigDecimal("500.750000"), result);
    }

    @Test
    void testResolveAmount_FixedAmount_Null() {
        // 测试固定金额解析 - null值
        BigDecimal result = ErpFinanceVoucherAmountSourceEnum.FIXED_AMOUNT.resolveAmount(null, null);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testResolveAmount_BizAmountRate() {
        // 测试业务金额比例解析
        BigDecimal bizAmount = new BigDecimal("1000");
        BigDecimal rate = new BigDecimal("0.15");
        BigDecimal result = ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT_RATE.resolveAmount(rate, bizAmount);
        assertEquals(new BigDecimal("150.000000"), result);
    }

    @Test
    void testResolveAmount_BizAmountRate_NullValues() {
        // 测试业务金额比例解析 - null值
        BigDecimal result = ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT_RATE.resolveAmount(null, null);
        assertEquals(new BigDecimal("0.000000"), result);
    }

    @Test
    void testResolveAmount_ResearchExpenseCategory() {
        // 测试研发支出分类金额解析
        BigDecimal bizAmount = new BigDecimal("2000.123");
        BigDecimal result = ErpFinanceVoucherAmountSourceEnum.RESEARCH_EXPENSE_CATEGORY.resolveAmount(null, bizAmount);
        assertEquals(new BigDecimal("2000.123000"), result);
    }

    @Test
    void testResolveAmount_ResearchExpenseCategory_Null() {
        // 测试研发支出分类金额解析 - null值
        BigDecimal result = ErpFinanceVoucherAmountSourceEnum.RESEARCH_EXPENSE_CATEGORY.resolveAmount(null, null);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testResolveAmount_ResearchProjectSummary() {
        // 测试研发项目汇总金额解析
        BigDecimal bizAmount = new BigDecimal("3500.567");
        BigDecimal result = ErpFinanceVoucherAmountSourceEnum.RESEARCH_PROJECT_SUMMARY.resolveAmount(null, bizAmount);
        assertEquals(new BigDecimal("3500.567000"), result);
    }

    @Test
    void testResolveAmount_ResearchProjectSummary_Null() {
        // 测试研发项目汇总金额解析 - null值
        BigDecimal result = ErpFinanceVoucherAmountSourceEnum.RESEARCH_PROJECT_SUMMARY.resolveAmount(null, null);
        assertEquals(BigDecimal.ZERO, result);
    }
}