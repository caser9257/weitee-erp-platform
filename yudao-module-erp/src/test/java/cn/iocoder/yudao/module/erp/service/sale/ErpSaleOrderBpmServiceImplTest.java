package cn.iocoder.yudao.module.erp.service.sale;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderAuditLogDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderAuditLogMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.ErpSaleOrderAuditActionTypeConstants;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErpSaleOrderBpmServiceImplTest {

    @Test
    void submitSaleOrder_shouldStartProcessAndBindProcessInstance() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpSaleOrderDO> saleOrderRef = new AtomicReference<>(
                new ErpSaleOrderDO()
                        .setId(11L)
                        .setNo("SO-2026-001")
                        .setStatus(ErpAuditStatus.PROCESS.getStatus())
                        .setCustomerId(201L)
                        .setProjectId(301L)
                        .setBusinessType("SELF_RESEARCH")
                        .setTotalPrice(new BigDecimal("1280.50"))
                        .setOrderTime(LocalDateTime.of(2026, 4, 7, 9, 0))
                        .setDeliveryDate(LocalDate.of(2026, 4, 10))
        );
        AtomicReference<ErpSaleOrderDO> updatedOrderRef = new AtomicReference<>();
        AtomicReference<Object> createReqRef = new AtomicReference<>();

        setField(service, "saleOrderMapper", createProxy(ErpSaleOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return saleOrderRef.get();
            }
            if ("updateById".equals(methodName)) {
                updatedOrderRef.set((ErpSaleOrderDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "processInstanceApi", createProxyByName(
                "cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi",
                (methodName, args) -> {
                    if ("createProcessInstance".equals(methodName)) {
                        createReqRef.set(args[1]);
                        return "PI-20260407-001";
                    }
                    return null;
                }));
        List<ErpSaleOrderAuditLogDO> auditLogs = new ArrayList<>();
        setField(service, "saleOrderAuditLogMapper", createProxy(ErpSaleOrderAuditLogMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                auditLogs.add((ErpSaleOrderAuditLogDO) args[0]);
                return 1;
            }
            return null;
        }));

        Object reqVO = createSubmitReqVO(11L, Map.of("task_1", List.of(7L, 8L)));
        Method method = service.getClass().getMethod("submitSaleOrder", Long.class, reqVO.getClass());

        Object result = method.invoke(service, 9527L, reqVO);

        assertEquals("PI-20260407-001", result);
        assertNotNull(createReqRef.get());
        assertEquals("11", readProperty(createReqRef.get(), "getBusinessKey"));
        Map<?, ?> variables = (Map<?, ?>) readProperty(createReqRef.get(), "getVariables");
        assertEquals("SO-2026-001", variables.get("saleOrderNo"));
        assertEquals(new BigDecimal("1280.50"), variables.get("totalPrice"));
        assertEquals(201L, variables.get("customerId"));
        assertEquals(301L, variables.get("projectId"));
        assertEquals("SELF_RESEARCH", variables.get("businessType"));
        assertNotNull(updatedOrderRef.get());
        assertEquals(11L, updatedOrderRef.get().getId());
        assertEquals("PI-20260407-001", updatedOrderRef.get().getProcessInstanceId());
        assertTrue(auditLogs.isEmpty());
    }

    @Test
    void submitSaleOrder_shouldWriteResubmitAuditLogWhenOrderWasRejected() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpSaleOrderDO> saleOrderRef = new AtomicReference<>(
                new ErpSaleOrderDO()
                        .setId(12L)
                        .setNo("SO-2026-REJECT")
                        .setStatus(ErpAuditStatus.REJECT.getStatus())
                        .setProcessInstanceId("OLD-PI")
                        .setCustomerId(202L)
                        .setTotalPrice(new BigDecimal("200"))
                        .setOrderTime(LocalDateTime.of(2026, 4, 7, 10, 0))
        );
        List<ErpSaleOrderAuditLogDO> auditLogs = new ArrayList<>();

        setField(service, "saleOrderMapper", createProxy(ErpSaleOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return saleOrderRef.get();
            }
            return 1;
        }));
        setField(service, "processInstanceApi", createProxyByName(
                "cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi",
                (methodName, args) -> "PI-NEW"));
        setField(service, "saleOrderAuditLogMapper", createProxy(ErpSaleOrderAuditLogMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                auditLogs.add((ErpSaleOrderAuditLogDO) args[0]);
                return 1;
            }
            return null;
        }));

        Object reqVO = createSubmitReqVO(12L, new HashMap<>());
        Method method = service.getClass().getMethod("submitSaleOrder", Long.class, reqVO.getClass());
        method.invoke(service, 9527L, reqVO);

        assertEquals(1, auditLogs.size());
        assertEquals(ErpSaleOrderAuditActionTypeConstants.RESUBMIT, auditLogs.get(0).getActionType());
        assertEquals(ErpAuditStatus.REJECT.getStatus(), auditLogs.get(0).getBeforeStatus());
        assertEquals(ErpAuditStatus.PROCESS.getStatus(), auditLogs.get(0).getAfterStatus());
    }

    @Test
    void cancelSaleOrderApproval_shouldCancelProcessAndClearBinding() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpSaleOrderDO> saleOrderRef = new AtomicReference<>(
                new ErpSaleOrderDO()
                        .setId(13L)
                        .setNo("SO-2026-CANCEL")
                        .setStatus(ErpAuditStatus.PROCESS.getStatus())
                        .setProcessInstanceId("PI-TO-CANCEL")
                        .setOutCount(BigDecimal.ZERO)
                        .setReturnCount(BigDecimal.ZERO)
        );
        AtomicReference<Long> clearedOrderIdRef = new AtomicReference<>();
        AtomicReference<Object> cancelReqRef = new AtomicReference<>();

        setField(service, "saleOrderMapper", createProxy(ErpSaleOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return saleOrderRef.get();
            }
            if ("clearProcessInstanceId".equals(methodName)) {
                clearedOrderIdRef.set((Long) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "processInstanceService", createProxyByName(
                "cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService",
                (methodName, args) -> {
                    if ("cancelProcessInstanceByStartUser".equals(methodName)) {
                        cancelReqRef.set(args[1]);
                    }
                    return null;
                }));

        Object reqVO = createCancelReqVO(13L, "cancel test");
        Method method = service.getClass().getMethod("cancelSaleOrderApproval", Long.class, reqVO.getClass());
        method.invoke(service, 9527L, reqVO);

        assertNotNull(cancelReqRef.get());
        assertEquals("PI-TO-CANCEL", readProperty(cancelReqRef.get(), "getId"));
        assertEquals("cancel test", readProperty(cancelReqRef.get(), "getReason"));
        assertEquals(13L, clearedOrderIdRef.get());
    }

    @Test
    void cancelSaleOrderApproval_shouldClearStaleBindingWhenProcessAlreadyStopped() throws Exception {
        Object service = instantiateService();
        Integer cancelStatus = bpmStatus("CANCEL");
        Object historicProcessInstance = createHistoricProcessInstance(cancelStatus, "cancelled");
        AtomicReference<ErpSaleOrderDO> saleOrderRef = new AtomicReference<>(
                new ErpSaleOrderDO()
                        .setId(15L)
                        .setNo("SO-2026-STALE")
                        .setStatus(ErpAuditStatus.PROCESS.getStatus())
                        .setProcessInstanceId("PI-ALREADY-CANCELLED")
        );
        AtomicReference<Long> clearedOrderIdRef = new AtomicReference<>();

        setField(service, "saleOrderMapper", createProxy(ErpSaleOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return saleOrderRef.get();
            }
            if ("clearProcessInstanceId".equals(methodName)) {
                clearedOrderIdRef.set((Long) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "processInstanceService", createProxyByName(
                "cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService",
                (methodName, args) -> {
                    if ("cancelProcessInstanceByStartUser".equals(methodName)) {
                        throw new ServiceException(1_009_004_001, "process is not running");
                    }
                    if ("getHistoricProcessInstance".equals(methodName)) {
                        return historicProcessInstance;
                    }
                    return null;
                }));

        Object reqVO = createCancelReqVO(15L, "cancel again");
        Method method = service.getClass().getMethod("cancelSaleOrderApproval", Long.class, reqVO.getClass());
        method.invoke(service, 9527L, reqVO);

        assertEquals(15L, clearedOrderIdRef.get());
    }

    @Test
    void handleProcessInstanceResult_shouldTranslateApproveAndIgnoreStaleProcessInstance() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpSaleOrderDO> saleOrderRef = new AtomicReference<>(
                new ErpSaleOrderDO().setId(14L).setProcessInstanceId("PI-MATCH")
        );
        AtomicReference<List<Object>> callbackArgsRef = new AtomicReference<>();

        setField(service, "saleOrderMapper", createProxy(ErpSaleOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return saleOrderRef.get();
            }
            return null;
        }));
        setField(service, "saleOrderService", createProxy(ErpSaleOrderService.class, (methodName, args) -> {
            if ("updateSaleOrderStatusByBpm".equals(methodName)) {
                callbackArgsRef.set(List.of(args));
            }
            return null;
        }));

        Method method = service.getClass().getMethod("handleProcessInstanceResult",
                Long.class, String.class, Integer.class, String.class);
        method.invoke(service, 14L, "PI-MATCH", bpmStatus("APPROVE"), "approved");

        assertNotNull(callbackArgsRef.get());
        assertEquals(14L, callbackArgsRef.get().get(0));
        assertEquals("PI-MATCH", callbackArgsRef.get().get(1));
        assertEquals(ErpAuditStatus.APPROVE.getStatus(), callbackArgsRef.get().get(2));
        assertEquals("approved", callbackArgsRef.get().get(3));

        callbackArgsRef.set(null);
        saleOrderRef.set(new ErpSaleOrderDO().setId(14L).setProcessInstanceId("PI-NEW"));
        method.invoke(service, 14L, "PI-OLD", bpmStatus("REJECT"), "stale");
        assertNull(callbackArgsRef.get());
    }

    private Object instantiateService() throws Exception {
        Class<?> clazz = Class.forName("cn.iocoder.yudao.module.erp.service.sale.ErpSaleOrderBpmServiceImpl");
        return clazz.getDeclaredConstructor().newInstance();
    }

    private Object createSubmitReqVO(Long id, Map<String, List<Long>> assignees) throws Exception {
        Class<?> clazz = Class.forName("cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderSubmitReqVO");
        Object reqVO = clazz.getDeclaredConstructor().newInstance();
        clazz.getMethod("setId", Long.class).invoke(reqVO, id);
        clazz.getMethod("setStartUserSelectAssignees", Map.class).invoke(reqVO, assignees);
        return reqVO;
    }

    private Object createCancelReqVO(Long id, String reason) throws Exception {
        Class<?> clazz = Class.forName("cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderCancelApprovalReqVO");
        Object reqVO = clazz.getDeclaredConstructor().newInstance();
        clazz.getMethod("setId", Long.class).invoke(reqVO, id);
        clazz.getMethod("setReason", String.class).invoke(reqVO, reason);
        return reqVO;
    }

    private Object createProxyByName(String className, MethodHandler handler) throws Exception {
        return createProxy(Class.forName(className), handler);
    }

    private Object createHistoricProcessInstance(Integer status, String reason) throws Exception {
        return createProxyByName("org.flowable.engine.history.HistoricProcessInstance", (methodName, args) -> {
            if ("getProcessVariables".equals(methodName)) {
                Map<String, Object> variables = new HashMap<>();
                variables.put("PROCESS_STATUS", status);
                variables.put("PROCESS_REASON", reason);
                return variables;
            }
            return null;
        });
    }

    private Object readProperty(Object target, String methodName) throws Exception {
        return target.getClass().getMethod(methodName).invoke(target);
    }

    private Integer bpmStatus(String enumName) throws Exception {
        Class<?> clazz = Class.forName("cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum");
        Object enumObj = Enum.valueOf((Class<Enum>) clazz.asSubclass(Enum.class), enumName);
        return (Integer) clazz.getMethod("getStatus").invoke(enumObj);
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
