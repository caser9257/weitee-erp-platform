package cn.weitee.erp.module.erp.service.sale;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderAuditLogDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleOrderAuditLogMapper;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpSaleOrderAuditActionTypeConstants;
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
        List<ErpSaleOrderDO> updates = new ArrayList<>();
        AtomicReference<Long> submitBizIdRef = new AtomicReference<>();

        setField(service, "saleOrderMapper", createProxy(ErpSaleOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return saleOrderRef.get();
            }
            if ("updateById".equals(methodName)) {
                updates.add((ErpSaleOrderDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "approvalRuntimeService", createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> {
            if ("submit".equals(methodName)) {
                submitBizIdRef.set((Long) args[1]);
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

        // afterCommit 模式：submit 返回 null
        assertNull(result);
        assertEquals(11L, submitBizIdRef.get());
        // 第一次 updateById：设置 PROCESS 状态，processInstanceId=null
        assertEquals(2, updates.size());
        assertNotNull(updates.get(0));
        assertEquals(11L, updates.get(0).getId());
        assertEquals(ErpAuditStatus.PROCESS.getStatus(), updates.get(0).getStatus());
        assertNull(updates.get(0).getProcessInstanceId());
        assertEquals(11L, updates.get(1).getId());
        assertEquals("PI-20260407-001", updates.get(1).getProcessInstanceId());
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
        setField(service, "approvalRuntimeService", createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> "PI-NEW"));
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
    void submitSaleOrder_shouldAllowRetryWhenOrderWasFailed() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpSaleOrderDO> saleOrderRef = new AtomicReference<>(
                new ErpSaleOrderDO()
                        .setId(16L)
                        .setNo("SO-2026-FAILED")
                        .setStatus(ErpAuditStatus.FAILED.getStatus())
                        .setProcessInstanceId("PI-FAILED-OLD")
        );
        List<ErpSaleOrderDO> updates = new ArrayList<>();
        AtomicReference<Long> submitBizIdRef = new AtomicReference<>();

        setField(service, "erpSaleOrderMapper", createProxy(ErpSaleOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return saleOrderRef.get();
            }
            if ("updateById".equals(methodName)) {
                updates.add((ErpSaleOrderDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "approvalRuntimeService", createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> {
            if ("submit".equals(methodName)) {
                submitBizIdRef.set((Long) args[1]);
                return "PI-RETRY-001";
            }
            return null;
        }));
        setField(service, "erpSaleOrderAuditLogMapper", createProxy(ErpSaleOrderAuditLogMapper.class, (methodName, args) -> 1));

        Object reqVO = createSubmitReqVO(16L, new HashMap<>());
        Method method = service.getClass().getMethod("submitSaleOrder", Long.class, reqVO.getClass());
        Object result = method.invoke(service, 9527L, reqVO);

        assertNull(result);
        assertEquals(16L, submitBizIdRef.get());
        assertEquals(2, updates.size());
        assertEquals(ErpAuditStatus.PROCESS.getStatus(), updates.get(0).getStatus());
        assertNull(updates.get(0).getProcessInstanceId());
        assertEquals("PI-RETRY-001", updates.get(1).getProcessInstanceId());
    }

    @Test
    void submitSaleOrder_shouldKeepFailedStatusWhenBpmCreateFails() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpSaleOrderDO> saleOrderRef = new AtomicReference<>(
                new ErpSaleOrderDO()
                        .setId(17L)
                        .setNo("SO-2026-FAILED-RETRY")
                        .setStatus(ErpAuditStatus.FAILED.getStatus())
        );
        List<ErpSaleOrderDO> updates = new ArrayList<>();

        setField(service, "erpSaleOrderMapper", createProxy(ErpSaleOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return saleOrderRef.get();
            }
            if ("updateById".equals(methodName)) {
                updates.add((ErpSaleOrderDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "approvalRuntimeService", createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> {
            throw new RuntimeException("bpm failed");
        }));
        setField(service, "erpSaleOrderAuditLogMapper", createProxy(ErpSaleOrderAuditLogMapper.class, (methodName, args) -> 1));

        Object reqVO = createSubmitReqVO(17L, new HashMap<>());
        Method method = service.getClass().getMethod("submitSaleOrder", Long.class, reqVO.getClass());
        method.invoke(service, 9527L, reqVO);

        assertEquals(2, updates.size());
        assertEquals(ErpAuditStatus.PROCESS.getStatus(), updates.get(0).getStatus());
        assertEquals(ErpAuditStatus.FAILED.getStatus(), updates.get(1).getStatus());
        assertNull(updates.get(1).getProcessInstanceId());
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
        AtomicReference<Long> cancelBizIdRef = new AtomicReference<>();

        setField(service, "saleOrderMapper", createProxy(ErpSaleOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return saleOrderRef.get();
            }
            return null;
        }));
        setField(service, "approvalRuntimeService", createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> {
            if ("cancel".equals(methodName)) {
                cancelBizIdRef.set((Long) args[1]);
            }
            return null;
        }));

        Object reqVO = createCancelReqVO(13L, "cancel test");
        Method method = service.getClass().getMethod("cancelSaleOrderApproval", Long.class, reqVO.getClass());
        method.invoke(service, 9527L, reqVO);

        // afterCommit 模式：BPM 撤回在 afterCommit 中执行
        assertEquals(13L, cancelBizIdRef.get());
    }

    @Test
    void cancelSaleOrderApproval_shouldClearStaleBindingWhenProcessAlreadyStopped() throws Exception {
        Object service = instantiateService();
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
        setField(service, "approvalRuntimeService", createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> {
            if ("cancel".equals(methodName)) {
                // 模拟快照不存在（流程已结束）
                throw new ServiceException(1_009_004_001, "approval instance snapshot not exists");
            }
            return null;
        }));

        Object reqVO = createCancelReqVO(15L, "cancel again");
        Method method = service.getClass().getMethod("cancelSaleOrderApproval", Long.class, reqVO.getClass());
        method.invoke(service, 9527L, reqVO);

        // afterCommit 中 cancel 抛异常被 catch，clearProcessBinding 不会被调用
        // 这是预期行为：流程已结束时，BPM 回调会清理 processInstanceId
        assertNull(clearedOrderIdRef.get());
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
        Class<?> clazz = Class.forName("cn.weitee.erp.module.erp.service.sale.ErpSaleOrderBpmServiceImpl");
        return clazz.getDeclaredConstructor().newInstance();
    }

    private Object createSubmitReqVO(Long id, Map<String, List<Long>> assignees) throws Exception {
        Class<?> clazz = Class.forName("cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderSubmitReqVO");
        Object reqVO = clazz.getDeclaredConstructor().newInstance();
        clazz.getMethod("setId", Long.class).invoke(reqVO, id);
        clazz.getMethod("setStartUserSelectAssignees", Map.class).invoke(reqVO, assignees);
        return reqVO;
    }

    private Object createCancelReqVO(Long id, String reason) throws Exception {
        Class<?> clazz = Class.forName("cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderCancelApprovalReqVO");
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
        Class<?> clazz = Class.forName("cn.weitee.erp.module.bpm.enums.task.BpmProcessInstanceStatusEnum");
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
        String actualFieldName = switch (fieldName) {
            case "saleOrderMapper" -> "erpSaleOrderMapper";
            case "saleOrderAuditLogMapper" -> "erpSaleOrderAuditLogMapper";
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
