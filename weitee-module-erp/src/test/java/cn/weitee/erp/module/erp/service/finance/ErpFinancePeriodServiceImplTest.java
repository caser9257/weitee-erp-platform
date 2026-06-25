package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.period.ErpFinancePeriodCreateYearReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePeriodDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePeriodMapper;
import cn.weitee.erp.module.erp.enums.ErpFinancePeriodStatusEnum;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpFinancePeriodServiceImplTest {

    @Test
    void createFinancePeriodsByYear_shouldOnlyInsertMissingMonths() throws Exception {
        ErpFinancePeriodServiceImpl service = new ErpFinancePeriodServiceImpl();
        AtomicReference<List<ErpFinancePeriodDO>> insertedPeriodsRef = new AtomicReference<>();

        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("validateFinanceLedger".equals(methodName)) {
                return new ErpFinanceLedgerDO()
                        .setId(1L)
                        .setName("标准账簿")
                        .setStatus(CommonStatusEnum.ENABLE.getStatus());
            }
            return null;
        }));
        setField(service, "financePeriodMapper", createProxy(ErpFinancePeriodMapper.class, (methodName, args) -> {
            if ("selectListByLedgerIdAndYear".equals(methodName)) {
                return List.of(new ErpFinancePeriodDO().setLedgerId(1L).setPeriodYear(2026).setPeriodMonth(1).setPeriodSort(202601));
            }
            if ("insertBatch".equals(methodName)) {
                insertedPeriodsRef.set((List<ErpFinancePeriodDO>) args[0]);
                return true;
            }
            return null;
        }));

        ErpFinancePeriodCreateYearReqVO reqVO = new ErpFinancePeriodCreateYearReqVO();
        reqVO.setLedgerId(1L);
        reqVO.setPeriodYear(2026);
        reqVO.setRemark("年度初始化");

        Integer count = service.createFinancePeriodsByYear(reqVO);

        assertEquals(11, count);
        assertEquals(11, insertedPeriodsRef.get().size());
        assertEquals("2026-02", insertedPeriodsRef.get().get(0).getPeriodCode());
    }

    @Test
    void closeFinancePeriod_shouldRejectWhenEarlierPeriodStillOpen() throws Exception {
        ErpFinancePeriodServiceImpl service = new ErpFinancePeriodServiceImpl();
        ErpFinancePeriodDO period = new ErpFinancePeriodDO()
                .setId(3L)
                .setLedgerId(1L)
                .setPeriodCode("2026-04")
                .setPeriodSort(202604)
                .setStatus(ErpFinancePeriodStatusEnum.OPEN.getStatus());

        setField(service, "financePeriodMapper", createProxy(ErpFinancePeriodMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return period;
            }
            if ("selectEarlierOpenCount".equals(methodName)) {
                return 1L;
            }
            return null;
        }));
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> null));

        assertThrows(RuntimeException.class, () -> service.closeFinancePeriod(3L));
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
