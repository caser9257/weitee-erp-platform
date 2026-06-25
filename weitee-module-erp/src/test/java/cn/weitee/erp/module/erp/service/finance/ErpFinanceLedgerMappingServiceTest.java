package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerMappingDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceLedgerMappingMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ErpFinanceLedgerMappingServiceTest {

    @Test
    void createLedgerMapping_shouldReturnId() throws Exception {
        ErpFinanceLedgerMappingServiceImpl service = new ErpFinanceLedgerMappingServiceImpl();
        AtomicReference<ErpFinanceLedgerMappingDO> insertedMappingRef = new AtomicReference<>();

        setField(service, "ledgerMappingMapper", createProxy(ErpFinanceLedgerMappingMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                ErpFinanceLedgerMappingDO mapping = (ErpFinanceLedgerMappingDO) args[0];
                mapping.setId(1L);
                insertedMappingRef.set(mapping);
                return 1;
            }
            return null;
        }));

        ErpFinanceLedgerMappingDO mapping = ErpFinanceLedgerMappingDO.builder()
                .externalLedgerId(100L)
                .internalLedgerId(200L)
                .mappingType("AUTO")
                .mappingRule("{\"rule\": \"test\"}")
                .status(0)
                .remark("测试映射")
                .build();

        Long id = service.createLedgerMapping(mapping);

        assertEquals(1L, id);
        assertNotNull(insertedMappingRef.get());
        assertEquals(100L, insertedMappingRef.get().getExternalLedgerId());
        assertEquals(200L, insertedMappingRef.get().getInternalLedgerId());
    }

    @Test
    void getLedgerMappingsByExternalLedger_shouldReturnList() throws Exception {
        ErpFinanceLedgerMappingServiceImpl service = new ErpFinanceLedgerMappingServiceImpl();
        List<ErpFinanceLedgerMappingDO> mockList = new ArrayList<>();
        mockList.add(ErpFinanceLedgerMappingDO.builder()
                .id(1L)
                .externalLedgerId(100L)
                .internalLedgerId(200L)
                .mappingType("AUTO")
                .build());

        setField(service, "ledgerMappingMapper", createProxy(ErpFinanceLedgerMappingMapper.class, (methodName, args) -> {
            if ("selectList".equals(methodName)) {
                return mockList;
            }
            return null;
        }));

        List<ErpFinanceLedgerMappingDO> result = service.getLedgerMappingsByExternalLedger(100L);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getExternalLedgerId());
    }

    @Test
    void getLedgerMappingByInternalLedger_shouldReturnMapping() throws Exception {
        ErpFinanceLedgerMappingServiceImpl service = new ErpFinanceLedgerMappingServiceImpl();
        ErpFinanceLedgerMappingDO mockMapping = ErpFinanceLedgerMappingDO.builder()
                .id(1L)
                .externalLedgerId(100L)
                .internalLedgerId(200L)
                .mappingType("AUTO")
                .build();

        setField(service, "ledgerMappingMapper", createProxy(ErpFinanceLedgerMappingMapper.class, (methodName, args) -> {
            if ("selectOne".equals(methodName)) {
                return mockMapping;
            }
            return null;
        }));

        ErpFinanceLedgerMappingDO result = service.getLedgerMappingByInternalLedger(200L);

        assertNotNull(result);
        assertEquals(200L, result.getInternalLedgerId());
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @SuppressWarnings("unchecked")
    private static <T> T createProxy(Class<T> interfaceType, java.util.function.BiFunction<String, Object[], Object> handler) {
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{interfaceType},
                (proxy, method, args) -> handler.apply(method.getName(), args)
        );
    }
}