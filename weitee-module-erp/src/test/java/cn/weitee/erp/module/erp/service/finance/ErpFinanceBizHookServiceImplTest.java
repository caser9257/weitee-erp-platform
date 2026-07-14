package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePeriodDO;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_PERIOD_CURRENT_OPEN_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpFinanceBizHookServiceImplTest {

    @Test
    void handleApprovedBiz_shouldStillSkipWhenNoLedgerAndVoucherServiceReturnsNull() throws Exception {
        ErpFinanceBizHookServiceImpl service = new ErpFinanceBizHookServiceImpl();
        AtomicReference<LocalDate> validatedDateRef = new AtomicReference<>();
        AtomicReference<Integer> generatedBizTypeRef = new AtomicReference<>();

        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("getDefaultFinanceLedger".equals(methodName)) {
                return null;
            }
            return null;
        }));
        setField(service, "financePeriodService", createProxy(ErpFinancePeriodService.class, (methodName, args) -> {
            if ("getCurrentOpenPeriod".equals(methodName)) {
                validatedDateRef.set((LocalDate) args[1]);
            }
            return null;
        }));
        setField(service, "financeVoucherService", createProxy(ErpFinanceVoucherService.class, (methodName, args) -> {
            if ("autoGenerateVoucher".equals(methodName)) {
                generatedBizTypeRef.set((Integer) args[0]);
                return null;
            }
            return null;
        }));

        Long result = service.handleApprovedBiz(ErpBizTypeEnum.FINANCE_EXPENSE.getType(), 100L,
                LocalDate.of(2026, 4, 29));

        assertNull(result);
        assertNull(validatedDateRef.get());
        assertEquals(ErpBizTypeEnum.FINANCE_EXPENSE.getType(), generatedBizTypeRef.get());
    }

    @Test
    void handleApprovedBiz_shouldNotDependOnDualWriteAfterAutoGeneratingVoucher() throws Exception {
        ErpFinanceBizHookServiceImpl service = new ErpFinanceBizHookServiceImpl();
        AtomicLong validatedLedgerIdRef = new AtomicLong();
        AtomicReference<LocalDate> validatedDateRef = new AtomicReference<>();
        AtomicReference<Integer> generatedBizTypeRef = new AtomicReference<>();
        AtomicReference<Long> generatedBizIdRef = new AtomicReference<>();

        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("getDefaultFinanceLedger".equals(methodName)) {
                return new ErpFinanceLedgerDO().setId(9L).setName("默认账簿")
                        .setStatus(CommonStatusEnum.ENABLE.getStatus());
            }
            return null;
        }));
        setField(service, "financePeriodService", createProxy(ErpFinancePeriodService.class, (methodName, args) -> {
            if ("getCurrentOpenPeriod".equals(methodName)) {
                validatedLedgerIdRef.set((Long) args[0]);
                validatedDateRef.set((LocalDate) args[1]);
                return new ErpFinancePeriodDO().setId(21L).setLedgerId(9L).setPeriodCode("2026-04");
            }
            return null;
        }));
        setField(service, "financeVoucherService", createProxy(ErpFinanceVoucherService.class, (methodName, args) -> {
            if ("autoGenerateVoucher".equals(methodName)) {
                generatedBizTypeRef.set((Integer) args[0]);
                generatedBizIdRef.set((Long) args[1]);
                return 88L;
            }
            return null;
        }));

        Long result = service.handleApprovedBiz(ErpBizTypeEnum.PURCHASE_IN.getType(), 11L,
                LocalDate.of(2026, 4, 30));

        assertEquals(88L, result);
        assertEquals(9L, validatedLedgerIdRef.get());
        assertEquals(LocalDate.of(2026, 4, 30), validatedDateRef.get());
        assertEquals(ErpBizTypeEnum.PURCHASE_IN.getType(), generatedBizTypeRef.get());
        assertEquals(11L, generatedBizIdRef.get());
        assertThrows(NoSuchFieldException.class,
                () -> ErpFinanceBizHookServiceImpl.class.getDeclaredField("dualWriteService"));
    }

    @Test
    void handleApprovedBiz_shouldUseCurrentDateWhenBizDateMissing() throws Exception {
        ErpFinanceBizHookServiceImpl service = new ErpFinanceBizHookServiceImpl();
        AtomicReference<LocalDate> validatedDateRef = new AtomicReference<>();

        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("getDefaultFinanceLedger".equals(methodName)) {
                return new ErpFinanceLedgerDO().setId(9L).setName("榛樿璐︾翱")
                        .setStatus(CommonStatusEnum.ENABLE.getStatus());
            }
            return null;
        }));
        setField(service, "financePeriodService", createProxy(ErpFinancePeriodService.class, (methodName, args) -> {
            if ("getCurrentOpenPeriod".equals(methodName)) {
                validatedDateRef.set((LocalDate) args[1]);
                return new ErpFinancePeriodDO().setId(21L).setLedgerId(9L).setPeriodCode("2026-04");
            }
            return null;
        }));
        setField(service, "financeVoucherService", createProxy(ErpFinanceVoucherService.class, (methodName, args) -> 1L));

        service.handleApprovedBiz(ErpBizTypeEnum.SALE_OUT.getType(), 12L, null);

        assertEquals(LocalDate.now(), validatedDateRef.get());
    }

    @Test
    void handleApprovedBiz_shouldAutoGenerateWhenDualLedgerFlowDoesNotNeedDefaultLedger() throws Exception {
        ErpFinanceBizHookServiceImpl service = new ErpFinanceBizHookServiceImpl();
        AtomicReference<Integer> generatedBizTypeRef = new AtomicReference<>();
        AtomicReference<Long> generatedBizIdRef = new AtomicReference<>();

        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("getDefaultFinanceLedger".equals(methodName)) {
                return null;
            }
            return null;
        }));
        setField(service, "financePeriodService", createProxy(ErpFinancePeriodService.class, (methodName, args) -> {
            if ("getCurrentOpenPeriod".equals(methodName)) {
                throw new IllegalStateException("dual ledger path should validate periods inside voucher service");
            }
            return null;
        }));
        setField(service, "financeVoucherService", createProxy(ErpFinanceVoucherService.class, (methodName, args) -> {
            if ("autoGenerateVoucher".equals(methodName)) {
                generatedBizTypeRef.set((Integer) args[0]);
                generatedBizIdRef.set((Long) args[1]);
                return 99L;
            }
            return null;
        }));

        Long result = service.handleApprovedBiz(ErpBizTypeEnum.FINANCE_EXPENSE.getType(), 100L,
                LocalDate.of(2026, 4, 29));

        assertEquals(99L, result);
        assertEquals(ErpBizTypeEnum.FINANCE_EXPENSE.getType(), generatedBizTypeRef.get());
        assertEquals(100L, generatedBizIdRef.get());
    }

    @Test
    void handleApprovedBiz_shouldBlockWhenCurrentOpenPeriodMissing() throws Exception {
        ErpFinanceBizHookServiceImpl service = new ErpFinanceBizHookServiceImpl();
        AtomicReference<Integer> generatedBizTypeRef = new AtomicReference<>();

        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("getDefaultFinanceLedger".equals(methodName)) {
                return new ErpFinanceLedgerDO().setId(9L).setName("榛樿璐︾翱")
                        .setStatus(CommonStatusEnum.ENABLE.getStatus());
            }
            return null;
        }));
        setField(service, "financePeriodService", createProxy(ErpFinancePeriodService.class, (methodName, args) -> {
            if ("getCurrentOpenPeriod".equals(methodName)) {
                throw new ServiceException(FINANCE_PERIOD_CURRENT_OPEN_NOT_EXISTS.getCode(),
                        FINANCE_PERIOD_CURRENT_OPEN_NOT_EXISTS.getMsg());
            }
            return null;
        }));
        setField(service, "financeVoucherService", createProxy(ErpFinanceVoucherService.class, (methodName, args) -> {
            if ("autoGenerateVoucher".equals(methodName)) {
                generatedBizTypeRef.set((Integer) args[0]);
            }
            return null;
        }));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.handleApprovedBiz(ErpBizTypeEnum.SALE_RETURN.getType(), 13L, LocalDate.of(2026, 4, 30)));

        assertEquals(FINANCE_PERIOD_CURRENT_OPEN_NOT_EXISTS.getCode(), ex.getCode());
        assertNull(generatedBizTypeRef.get());
    }

    @Test
    void handleRollbackBiz_shouldDelegateToVoucherService() throws Exception {
        ErpFinanceBizHookServiceImpl service = new ErpFinanceBizHookServiceImpl();
        AtomicReference<Integer> rollbackBizTypeRef = new AtomicReference<>();
        AtomicReference<Long> rollbackBizIdRef = new AtomicReference<>();
        AtomicReference<Long> rollbackUserIdRef = new AtomicReference<>();
        AtomicReference<String> rollbackRemarkRef = new AtomicReference<>();

        setField(service, "financeVoucherService", createProxy(ErpFinanceVoucherService.class, (methodName, args) -> {
            if ("rollbackAutoGeneratedVoucher".equals(methodName)) {
                rollbackBizTypeRef.set((Integer) args[0]);
                rollbackBizIdRef.set((Long) args[1]);
                rollbackUserIdRef.set((Long) args[2]);
                rollbackRemarkRef.set((String) args[3]);
            }
            return null;
        }));

        service.handleRollbackBiz(ErpBizTypeEnum.PURCHASE_IN.getType(), 22L, 9L, "反审核回滚");

        assertEquals(ErpBizTypeEnum.PURCHASE_IN.getType(), rollbackBizTypeRef.get());
        assertEquals(22L, rollbackBizIdRef.get());
        assertEquals(9L, rollbackUserIdRef.get());
        assertEquals("反审核回滚", rollbackRemarkRef.get());
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
        Object handle(String methodName, Object[] args);
    }
}
