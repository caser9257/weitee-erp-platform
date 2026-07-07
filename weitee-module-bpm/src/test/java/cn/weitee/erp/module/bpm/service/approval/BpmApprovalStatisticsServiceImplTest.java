package cn.weitee.erp.module.bpm.service.approval;

import cn.weitee.erp.module.bpm.controller.admin.approval.vo.statistics.ApprovalStatisticsRespVO;
import cn.weitee.erp.module.bpm.dal.mysql.approval.BpmApprovalInstanceSnapshotMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BpmApprovalStatisticsServiceImplTest {

    @Test
    void getCurrentApprovalStatistics_shouldUseEffectiveStatisticsQuery() throws Exception {
        BpmApprovalStatisticsServiceImpl service = new BpmApprovalStatisticsServiceImpl();
        AtomicBoolean effectiveQueryCalledRef = new AtomicBoolean(false);

        setField(service, "approvalInstanceSnapshotMapper",
                createProxy(BpmApprovalInstanceSnapshotMapper.class, (methodName, args) -> {
                    if ("selectEffectiveStatusStatistics".equals(methodName)) {
                        effectiveQueryCalledRef.set(true);
                        return Map.of(
                                "processingCount", 6L,
                                "approvedCount", 2L,
                                "rejectedCount", 1L,
                                "cancelledCount", 0L
                        );
                    }
                    if ("selectStatusStatistics".equals(methodName)) {
                        throw new IllegalStateException("historical statistics query should not be used");
                    }
                    return null;
                }));

        ApprovalStatisticsRespVO respVO = service.getCurrentApprovalStatistics();

        assertTrue(effectiveQueryCalledRef.get());
        assertEquals(9L, respVO.getTotalCount());
        assertEquals(6L, respVO.getProcessingCount());
        assertEquals(2L, respVO.getApprovedCount());
        assertEquals(1L, respVO.getRejectedCount());
        assertEquals(0L, respVO.getCancelledCount());
    }

    @Test
    void getApprovalStatistics_shouldKeepHistoricalStatisticsQuery() throws Exception {
        BpmApprovalStatisticsServiceImpl service = new BpmApprovalStatisticsServiceImpl();

        setField(service, "approvalInstanceSnapshotMapper",
                createProxy(BpmApprovalInstanceSnapshotMapper.class, (methodName, args) -> {
                    if ("selectStatusStatistics".equals(methodName)) {
                        return Map.of(
                                "processingCount", 6L,
                                "approvedCount", 0L,
                                "rejectedCount", 0L,
                                "cancelledCount", 9L
                        );
                    }
                    if ("selectEffectiveStatusStatistics".equals(methodName)) {
                        throw new IllegalStateException("effective statistics query should not be used");
                    }
                    return null;
                }));

        ApprovalStatisticsRespVO respVO = service.getApprovalStatistics();

        assertEquals(15L, respVO.getTotalCount());
        assertEquals(9L, respVO.getCancelledCount());
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
