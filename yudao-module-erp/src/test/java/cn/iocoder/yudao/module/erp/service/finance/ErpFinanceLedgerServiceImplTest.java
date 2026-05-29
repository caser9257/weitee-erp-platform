package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.ledger.ErpFinanceLedgerSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceLedgerMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinancePeriodMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_LEDGER_DEFAULT_DUPLICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpFinanceLedgerServiceImplTest {

    @Test
    void createFinanceLedger_shouldUnsetExistingDefaultLedger() throws Exception {
        ErpFinanceLedgerServiceImpl service = new ErpFinanceLedgerServiceImpl();
        AtomicReference<ErpFinanceLedgerDO> updatedDefaultRef = new AtomicReference<>();
        AtomicReference<ErpFinanceLedgerDO> insertedLedgerRef = new AtomicReference<>();

        setField(service, "financeLedgerMapper", createProxy(ErpFinanceLedgerMapper.class, (methodName, args) -> {
            if ("selectByNo".equals(methodName)) {
                return null;
            }
            if ("selectByDefaultStatus".equals(methodName)) {
                return new ErpFinanceLedgerDO().setId(9L).setDefaultStatus(true);
            }
            if ("selectListByStatus".equals(methodName)) {
                return List.of(new ErpFinanceLedgerDO().setId(9L).setDefaultStatus(true)
                        .setStatus(CommonStatusEnum.ENABLE.getStatus()));
            }
            if ("updateById".equals(methodName)) {
                updatedDefaultRef.set((ErpFinanceLedgerDO) args[0]);
                return 1;
            }
            if ("insert".equals(methodName)) {
                ErpFinanceLedgerDO ledger = (ErpFinanceLedgerDO) args[0];
                ledger.setId(11L);
                insertedLedgerRef.set(ledger);
                return 1;
            }
            return null;
        }));
        setField(service, "financePeriodMapper", createProxy(ErpFinancePeriodMapper.class, (methodName, args) -> 0L));

        ErpFinanceLedgerSaveReqVO reqVO = new ErpFinanceLedgerSaveReqVO();
        reqVO.setNo("BOOK-STD");
        reqVO.setName("标准账簿");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setSort(10);
        reqVO.setDefaultStatus(true);

        Long id = service.createFinanceLedger(reqVO);

        assertEquals(11L, id);
        assertEquals(9L, updatedDefaultRef.get().getId());
        assertEquals(Boolean.TRUE, insertedLedgerRef.get().getDefaultStatus());
    }

    @Test
    void getDefaultFinanceLedger_shouldRejectWhenMultipleEnabledDefaultsExist() throws Exception {
        ErpFinanceLedgerServiceImpl service = new ErpFinanceLedgerServiceImpl();

        setField(service, "financeLedgerMapper", createProxy(ErpFinanceLedgerMapper.class, (methodName, args) -> {
            if ("selectListByStatus".equals(methodName)) {
                return List.of(
                        new ErpFinanceLedgerDO().setId(9L).setName("BOOK-A")
                                .setStatus(CommonStatusEnum.ENABLE.getStatus()).setDefaultStatus(true),
                        new ErpFinanceLedgerDO().setId(10L).setName("BOOK-B")
                                .setStatus(CommonStatusEnum.ENABLE.getStatus()).setDefaultStatus(true));
            }
            return null;
        }));
        setField(service, "financePeriodMapper", createProxy(ErpFinancePeriodMapper.class, (methodName, args) -> 0L));

        ServiceException ex = assertThrows(ServiceException.class, service::getDefaultFinanceLedger);

        assertEquals(FINANCE_LEDGER_DEFAULT_DUPLICATE.getCode(), ex.getCode());
    }

    @Test
    void updateFinanceLedgerDefaultStatus_shouldClearAllExistingDefaultLedgers() throws Exception {
        ErpFinanceLedgerServiceImpl service = new ErpFinanceLedgerServiceImpl();
        List<ErpFinanceLedgerDO> updatedLedgers = new ArrayList<>();

        setField(service, "financeLedgerMapper", createProxy(ErpFinanceLedgerMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpFinanceLedgerDO().setId(11L).setName("BOOK-C")
                        .setStatus(CommonStatusEnum.ENABLE.getStatus()).setDefaultStatus(false);
            }
            if ("selectListByStatus".equals(methodName)) {
                return List.of(
                        new ErpFinanceLedgerDO().setId(9L).setName("BOOK-A")
                                .setStatus(CommonStatusEnum.ENABLE.getStatus()).setDefaultStatus(true),
                        new ErpFinanceLedgerDO().setId(10L).setName("BOOK-B")
                                .setStatus(CommonStatusEnum.ENABLE.getStatus()).setDefaultStatus(true),
                        new ErpFinanceLedgerDO().setId(11L).setName("BOOK-C")
                                .setStatus(CommonStatusEnum.ENABLE.getStatus()).setDefaultStatus(false));
            }
            if ("updateById".equals(methodName)) {
                updatedLedgers.add((ErpFinanceLedgerDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "financePeriodMapper", createProxy(ErpFinancePeriodMapper.class, (methodName, args) -> 0L));

        service.updateFinanceLedgerDefaultStatus(11L, true);

        assertEquals(3, updatedLedgers.size());
        assertEquals(9L, updatedLedgers.get(0).getId());
        assertEquals(Boolean.FALSE, updatedLedgers.get(0).getDefaultStatus());
        assertEquals(10L, updatedLedgers.get(1).getId());
        assertEquals(Boolean.FALSE, updatedLedgers.get(1).getDefaultStatus());
        assertEquals(11L, updatedLedgers.get(2).getId());
        assertEquals(Boolean.TRUE, updatedLedgers.get(2).getDefaultStatus());
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
        String actualFieldName = switch (fieldName) {
            case "financeLedgerMapper" -> "erpFinanceLedgerMapper";
            case "financePeriodMapper" -> "erpFinancePeriodMapper";
            default -> fieldName;
        };
        Field field = target.getClass().getDeclaredField(actualFieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }
}
