package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceAssetDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePeriodDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceSubjectDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherEntryDO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpFinanceExpenseAccountingTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceExpenseTypeEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ErpFinanceRdMonthEndServiceImplTest {

    @Test
    void executeMonthEnd_shouldGenerateExpenseCarryForwardVoucherAndDepreciation() throws Exception {
        ErpFinanceRdMonthEndServiceImpl service = new ErpFinanceRdMonthEndServiceImpl();
        AtomicReference<List<Long>> depreciationAssetIdsRef = new AtomicReference<>();

        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("getDefaultFinanceLedger".equals(methodName)) {
                return new cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO().setId(99601L).setName("默认账簿");
            }
            if ("validateFinanceLedger".equals(methodName)) {
                return new cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO().setId(99601L).setName("默认账簿");
            }
            return null;
        }));
        setField(service, "financePeriodService", createProxy(ErpFinancePeriodService.class, (methodName, args) -> {
            if ("getCurrentOpenPeriod".equals(methodName)) {
                return new ErpFinancePeriodDO().setId(99616L).setLedgerId(99601L)
                        .setPeriodCode("2026-05")
                        .setStartDate(LocalDate.of(2026, 5, 1))
                        .setEndDate(LocalDate.of(2026, 5, 31));
            }
            return null;
        }));
        setField(service, "financeExpenseService", createProxy(ErpFinanceExpenseService.class, (methodName, args) -> {
            if ("getApprovedResearchExpenseListByMonth".equals(methodName)) {
                return List.of(
                        expense(101L, "FYBX-001", ErpFinanceExpenseAccountingTypeEnum.EXPENSE.getType(), "100.00"),
                        expense(102L, "FYBX-002", ErpFinanceExpenseAccountingTypeEnum.CAPITALIZE.getType(), "300.00"));
            }
            return null;
        }));
        setField(service, "financeVoucherService", createProxy(ErpFinanceVoucherService.class, (methodName, args) -> {
            if ("getVoucherByLedgerAndBiz".equals(methodName)) {
                Integer bizType = (Integer) args[1];
                Long bizId = (Long) args[2];
                if (ErpBizTypeEnum.FINANCE_EXPENSE_EXPENSE.getType().equals(bizType) && Long.valueOf(101L).equals(bizId)) {
                    return new ErpFinanceVoucherDO().setId(5001L).setLedgerId(99601L).setBizType(bizType).setBizId(bizId);
                }
                if (ErpBizTypeEnum.FINANCE_EXPENSE_CAPITALIZE.getType().equals(bizType) && Long.valueOf(102L).equals(bizId)) {
                    return new ErpFinanceVoucherDO().setId(5002L).setLedgerId(99601L).setBizType(bizType).setBizId(bizId);
                }
                if (ErpBizTypeEnum.FINANCE_EXPENSE.getType().equals(bizType) && Long.valueOf(202605L).equals(bizId)) {
                    return null;
                }
            }
            if ("createMonthEndVoucher".equals(methodName)) {
                Map<String, BigDecimal> subjectAmountMap = (Map<String, BigDecimal>) args[2];
                assertEquals(new BigDecimal("100.00"), subjectAmountMap.get("6601"));
                assertEquals("4103", args[3]);
                return 8801L;
            }
            return null;
        }));
        setField(service, "financeVoucherEntryMapper", createProxy(cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherEntryMapper.class, (methodName, args) -> {
            if ("selectListByVoucherIds".equals(methodName)) {
                return List.of(
                        new ErpFinanceVoucherEntryDO().setVoucherId(5001L).setSubjectCode("6601").setSubjectName("销售费用")
                                .setDebitAmount(new BigDecimal("100.00")).setCreditAmount(BigDecimal.ZERO),
                        new ErpFinanceVoucherEntryDO().setVoucherId(5001L).setSubjectCode("2202").setSubjectName("应付账款")
                                .setDebitAmount(BigDecimal.ZERO).setCreditAmount(new BigDecimal("100.00")));
            }
            return null;
        }));
        setField(service, "financeSubjectService", createProxy(ErpFinanceSubjectService.class, (methodName, args) -> {
            if ("getFinanceSubjectMapByLedgerIdAndSubjectCodes".equals(methodName)) {
                return Map.of(
                        "6601", new ErpFinanceSubjectDO().setId(1L).setLedgerId(99601L).setSubjectCode("6601").setSubjectName("销售费用"),
                        "4103", new ErpFinanceSubjectDO().setId(2L).setLedgerId(99601L).setSubjectCode("4103").setSubjectName("本年利润"));
            }
            return null;
        }));
        setField(service, "financeAssetService", createProxy(ErpFinanceAssetService.class, (methodName, args) -> {
            if ("getActiveResearchCapitalizeAssetIdsByPeriod".equals(methodName)) {
                depreciationAssetIdsRef.set(List.of(9001L));
                return List.of(9001L);
            }
            return null;
        }));
        setField(service, "financeAssetDepreciationService", createProxy(ErpFinanceAssetDepreciationService.class, (methodName, args) -> {
            if ("generateDepreciationForAssets".equals(methodName)) {
                assertEquals("2026-05", args[0]);
                assertEquals(List.of(9001L), args[1]);
                return 1;
            }
            return null;
        }));

        ErpFinanceRdMonthEndService.MonthEndResult result = service.executeMonthEnd("2026-05");

        assertEquals(8801L, result.getExpenseCarryForwardVoucherId());
        assertEquals(1, result.getCapitalizeDepreciationCount());
        assertEquals(List.of(9001L), depreciationAssetIdsRef.get());
    }

    @Test
    void executeMonthEnd_shouldReuseExistingExpenseCarryForwardVoucher() throws Exception {
        ErpFinanceRdMonthEndServiceImpl service = new ErpFinanceRdMonthEndServiceImpl();

        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("getDefaultFinanceLedger".equals(methodName) || "validateFinanceLedger".equals(methodName)) {
                return new cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO().setId(99601L).setName("默认账簿");
            }
            return null;
        }));
        setField(service, "financePeriodService", createProxy(ErpFinancePeriodService.class, (methodName, args) -> {
            if ("getCurrentOpenPeriod".equals(methodName)) {
                return new ErpFinancePeriodDO().setId(99616L).setLedgerId(99601L)
                        .setPeriodCode("2026-05")
                        .setStartDate(LocalDate.of(2026, 5, 1))
                        .setEndDate(LocalDate.of(2026, 5, 31));
            }
            return null;
        }));
        setField(service, "financeExpenseService", createProxy(ErpFinanceExpenseService.class, (methodName, args) -> List.of()));
        setField(service, "financeVoucherService", createProxy(ErpFinanceVoucherService.class, (methodName, args) -> {
            if ("getVoucherByLedgerAndBiz".equals(methodName)) {
                return new ErpFinanceVoucherDO().setId(9901L)
                        .setLedgerId(99601L)
                        .setBizType(ErpBizTypeEnum.FINANCE_EXPENSE.getType())
                        .setBizId(202605L);
            }
            if ("createMonthEndVoucher".equals(methodName)) {
                throw new AssertionError("不应重复创建月末结转凭证");
            }
            return null;
        }));
        setField(service, "financeVoucherEntryMapper", createProxy(cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherEntryMapper.class, (methodName, args) -> List.of()));
        setField(service, "financeSubjectService", createProxy(ErpFinanceSubjectService.class, (methodName, args) -> Map.of()));
        setField(service, "financeAssetService", createProxy(ErpFinanceAssetService.class, (methodName, args) -> List.of()));
        setField(service, "financeAssetDepreciationService", createProxy(ErpFinanceAssetDepreciationService.class, (methodName, args) -> 0));

        ErpFinanceRdMonthEndService.MonthEndResult result = service.executeMonthEnd("2026-05");

        assertEquals(9901L, result.getExpenseCarryForwardVoucherId());
        assertEquals(0, result.getCapitalizeDepreciationCount());
    }

    private ErpFinanceExpenseDO expense(Long id, String no, Integer rdAccountingType, String amount) {
        return new ErpFinanceExpenseDO()
                .setId(id)
                .setNo(no)
                .setStatus(ErpAuditStatus.APPROVE.getStatus())
                .setExpenseType(ErpFinanceExpenseTypeEnum.RESEARCH.getType())
                .setRdAccountingType(rdAccountingType)
                .setExpenseTime(LocalDateTime.of(2026, 5, 20, 10, 0))
                .setExpensePrice(new BigDecimal(amount));
    }

    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> type, MethodHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        if ("toString".equals(method.getName())) {
                            return type.getSimpleName() + "Proxy";
                        }
                        if ("hashCode".equals(method.getName())) {
                            return System.identityHashCode(proxy);
                        }
                        if ("equals".equals(method.getName())) {
                            return proxy == args[0];
                        }
                    }
                    return handler.handle(method.getName(), args);
                });
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args) throws Exception;
    }
}
