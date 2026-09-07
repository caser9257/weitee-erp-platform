package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.module.erp.controller.admin.finance.vo.prepayment.ErpFinancePrepaymentAllocateReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePrepaymentAllocateDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePrepaymentDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpApStatementItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePrepaymentAllocateMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePrepaymentMapper;
import cn.weitee.erp.module.erp.enums.ErpApStatementStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpFinancePrepaymentServiceImplTest {

    @Test
    void allocateFinancePrepayment_shouldPersistPrepaymentIdOnAllocation() throws Exception {
        AtomicReference<List<ErpFinancePrepaymentAllocateDO>> insertedAllocates = new AtomicReference<>();
        ErpFinancePrepaymentServiceImpl service = createService(new BigDecimal("100.00"), new BigDecimal("80.00"),
                new BigDecimal("80.00"), insertedAllocates);

        service.allocateFinancePrepayment(new ErpFinancePrepaymentAllocateReqVO()
                .setPrepaymentId(7L)
                .setItems(List.of(new ErpFinancePrepaymentAllocateReqVO.Item()
                        .setApStatementId(11L)
                        .setAllocateAmount(new BigDecimal("20.00"))
                        .setRemark("核销"))));

        assertEquals(7L, insertedAllocates.get().getFirst().getPrepaymentId());
    }

    @Test
    void allocateFinancePrepayment_shouldRejectAmountExceedingPrepaymentRemainPrice() throws Exception {
        AtomicReference<List<ErpFinancePrepaymentAllocateDO>> insertedAllocates = new AtomicReference<>();
        ErpFinancePrepaymentServiceImpl service = createService(new BigDecimal("100.00"), new BigDecimal("150.00"),
                new BigDecimal("150.00"), insertedAllocates);

        assertThrows(RuntimeException.class, () -> service.allocateFinancePrepayment(
                new ErpFinancePrepaymentAllocateReqVO()
                        .setPrepaymentId(7L)
                        .setItems(List.of(new ErpFinancePrepaymentAllocateReqVO.Item()
                                .setApStatementId(11L)
                                .setAllocateAmount(new BigDecimal("120.00"))))));

        assertNull(insertedAllocates.get());
    }

    @SuppressWarnings("unchecked")
    private static ErpFinancePrepaymentServiceImpl createService(BigDecimal prepaymentRemainPrice,
                                                                  BigDecimal statementAmount,
                                                                  BigDecimal statementRemainAmount,
                                                                  AtomicReference<List<ErpFinancePrepaymentAllocateDO>> insertedAllocates)
            throws Exception {
        ErpFinancePrepaymentServiceImpl service = new ErpFinancePrepaymentServiceImpl();
        ErpFinancePrepaymentDO prepayment = new ErpFinancePrepaymentDO()
                .setId(7L).setNo("YF-7").setSupplierId(9L)
                .setStatus(ErpAuditStatus.APPROVE.getStatus())
                .setPrepaymentPrice(new BigDecimal("100.00"))
                .setRemainPrice(prepaymentRemainPrice);
        ErpApStatementDO statement = new ErpApStatementDO()
                .setId(11L).setStatementNo("YF-11").setSupplierId(9L)
                .setStatus(ErpApStatementStatusEnum.UNPAID.getStatus())
                .setAmount(statementAmount)
                .setRemainAmount(statementRemainAmount);

        setField(service, "erpFinancePrepaymentMapper", createProxy(ErpFinancePrepaymentMapper.class,
                (methodName, args) -> "selectById".equals(methodName) ? prepayment : defaultValue(methodName)));
        setField(service, "erpFinancePrepaymentAllocateMapper", createProxy(ErpFinancePrepaymentAllocateMapper.class,
                (methodName, args) -> {
                    if ("insertBatch".equals(methodName)) {
                        insertedAllocates.set((List<ErpFinancePrepaymentAllocateDO>) args[0]);
                        return true;
                    }
                    if ("selectApprovedListByPrepaymentId".equals(methodName)) {
                        return List.of();
                    }
                    return defaultValue(methodName);
                }));
        setField(service, "erpApStatementItemMapper", createProxy(ErpApStatementItemMapper.class,
                (methodName, args) -> defaultValue(methodName)));
        setField(service, "apStatementService", createProxy(ErpApStatementService.class,
                (methodName, args) -> {
                    if ("validateApStatement".equals(methodName)) {
                        return statement;
                    }
                    if ("getApStatementListByIds".equals(methodName)) {
                        return List.of(statement);
                    }
                    return defaultValue(methodName);
                }));
        setField(service, "redissonClient", createProxy(RedissonClient.class,
                (methodName, args) -> "getLock".equals(methodName)
                        ? createProxy(RLock.class, (lockMethodName, lockArgs) -> switch (lockMethodName) {
                            case "tryLock" -> true;
                            case "isHeldByCurrentThread" -> true;
                            default -> null;
                        })
                        : null));
        setField(service, "transactionManager", createProxy(PlatformTransactionManager.class,
                (methodName, args) -> "getTransaction".equals(methodName) ? new SimpleTransactionStatus() : null));
        return service;
    }

    @SuppressWarnings("unchecked")
    private static <T> T createProxy(Class<T> type, MethodHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "toString" -> type.getSimpleName() + "Proxy";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> null;
                };
            }
            return handler.handle(method.getName(), args);
        });
    }

    private static Object defaultValue(String methodName) {
        return switch (methodName) {
            case "insert", "updateById" -> 1;
            default -> null;
        };
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
