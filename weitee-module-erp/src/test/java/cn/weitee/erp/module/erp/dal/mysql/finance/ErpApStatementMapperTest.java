package cn.weitee.erp.module.erp.dal.mysql.finance;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErpApStatementMapperTest {

    @Test
    void selectPage_shouldExcludeStatementsFullyAllocatedByPrepayment() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/cn/iocoder/weitee/module/erp/dal/mysql/finance/ErpApStatementMapper.java"));

        assertTrue(source.contains("buildExcludeFullyPrepaidSql()"));
        assertTrue(source.contains("IFNULL((SELECT SUM(allocate_amount)"));
        assertTrue(source.contains("(amount <= 0 OR"));
    }

    @Test
    void selectPaymentEnablePage_shouldRequirePositiveRemainAmount() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/cn/iocoder/weitee/module/erp/dal/mysql/finance/ErpApStatementMapper.java"));

        assertTrue(source.contains(".gt(ErpApStatementDO::getRemainAmount, BigDecimal.ZERO)"));
        assertFalse(source.contains(".ne(ErpApStatementDO::getRemainAmount, BigDecimal.ZERO)"));
    }
}
