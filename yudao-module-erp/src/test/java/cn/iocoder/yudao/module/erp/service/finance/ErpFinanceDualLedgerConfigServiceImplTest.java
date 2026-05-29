package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerConfigSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerConfigDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceDualLedgerConfigMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_DUAL_LEDGER_CONFIG_BIZ_TYPE_DUPLICATE;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsFinanceLedger.FINANCE_DUAL_LEDGER_CONFIG_LEDGER_SAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpFinanceDualLedgerConfigServiceImplTest {

    @Test
    void createDualLedgerConfig_shouldInsertWhenValid() throws Exception {
        ErpFinanceDualLedgerConfigServiceImpl service = new ErpFinanceDualLedgerConfigServiceImpl();
        AtomicReference<ErpFinanceDualLedgerConfigDO> insertedRef = new AtomicReference<>();

        setField(service, "erpFinanceDualLedgerConfigMapper", createProxy(ErpFinanceDualLedgerConfigMapper.class, (methodName, args) -> {
            if ("selectByBizType".equals(methodName)) {
                return null;
            }
            if ("insert".equals(methodName)) {
                ErpFinanceDualLedgerConfigDO config = (ErpFinanceDualLedgerConfigDO) args[0];
                config.setId(101L);
                insertedRef.set(config);
                return 1;
            }
            return null;
        }));
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("validateFinanceLedger".equals(methodName)) {
                Long id = (Long) args[0];
                return new ErpFinanceLedgerDO().setId(id).setName("LEDGER-" + id)
                        .setStatus(CommonStatusEnum.ENABLE.getStatus());
            }
            return null;
        }));

        ErpFinanceDualLedgerConfigSaveReqVO reqVO = new ErpFinanceDualLedgerConfigSaveReqVO();
        reqVO.setBizType(11);
        reqVO.setExternalLedgerId(99601L);
        reqVO.setInternalLedgerId(99602L);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setRemark("采购入库双账套映射");

        Long id = service.createDualLedgerConfig(reqVO);

        assertEquals(101L, id);
        assertEquals(11, insertedRef.get().getBizType().intValue());
        assertEquals(99601L, insertedRef.get().getExternalLedgerId().longValue());
        assertEquals(99602L, insertedRef.get().getInternalLedgerId().longValue());
    }

    @Test
    void createDualLedgerConfig_shouldRejectDuplicateBizType() throws Exception {
        ErpFinanceDualLedgerConfigServiceImpl service = new ErpFinanceDualLedgerConfigServiceImpl();

        setField(service, "erpFinanceDualLedgerConfigMapper", createProxy(ErpFinanceDualLedgerConfigMapper.class, (methodName, args) -> {
            if ("selectByBizType".equals(methodName)) {
                return new ErpFinanceDualLedgerConfigDO().setId(9L).setBizType(11);
            }
            return null;
        }));
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("validateFinanceLedger".equals(methodName)) {
                Long id = (Long) args[0];
                return new ErpFinanceLedgerDO().setId(id).setStatus(CommonStatusEnum.ENABLE.getStatus());
            }
            return null;
        }));

        ErpFinanceDualLedgerConfigSaveReqVO reqVO = new ErpFinanceDualLedgerConfigSaveReqVO();
        reqVO.setBizType(11);
        reqVO.setExternalLedgerId(99601L);
        reqVO.setInternalLedgerId(99602L);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createDualLedgerConfig(reqVO));

        assertEquals(FINANCE_DUAL_LEDGER_CONFIG_BIZ_TYPE_DUPLICATE.getCode(), ex.getCode());
    }

    @Test
    void createDualLedgerConfig_shouldRejectSameLedger() throws Exception {
        ErpFinanceDualLedgerConfigServiceImpl service = new ErpFinanceDualLedgerConfigServiceImpl();
        setField(service, "erpFinanceDualLedgerConfigMapper", createProxy(ErpFinanceDualLedgerConfigMapper.class,
                (methodName, args) -> null));
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class,
                (methodName, args) -> null));

        ErpFinanceDualLedgerConfigSaveReqVO reqVO = new ErpFinanceDualLedgerConfigSaveReqVO();
        reqVO.setBizType(11);
        reqVO.setExternalLedgerId(99601L);
        reqVO.setInternalLedgerId(99601L);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createDualLedgerConfig(reqVO));

        assertEquals(FINANCE_DUAL_LEDGER_CONFIG_LEDGER_SAME.getCode(), ex.getCode());
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
