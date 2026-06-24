package cn.iocoder.yudao.module.erp.service.purchase;

import cn.iocoder.yudao.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ErpPurchaseInBpmServiceImplTest {

    @Test
    void submitPurchaseIn_shouldStartProcessAndBindProcessInstance() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpPurchaseInDO> purchaseInRef = new AtomicReference<>(
                new ErpPurchaseInDO()
                        .setId(31L)
                        .setNo("PI-2026-001")
                        .setStatus(ErpAuditStatus.PROCESS.getStatus())
                        .setSupplierId(301L)
                        .setOrderId(501L)
                        .setTotalPrice(new BigDecimal("1880.66"))
                        .setInTime(LocalDateTime.of(2026, 4, 9, 10, 0))
        );
        List<ErpPurchaseInDO> updatedPurchaseIns = new ArrayList<>();
        AtomicReference<Long> submitBizIdRef = new AtomicReference<>();

        setField(service, "purchaseInMapper", createProxy(ErpPurchaseInMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return purchaseInRef.get();
            }
            if ("updateById".equals(methodName)) {
                updatedPurchaseIns.add((ErpPurchaseInDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "approvalRuntimeService", createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> {
            if ("submit".equals(methodName)) {
                submitBizIdRef.set((Long) args[1]);
                return "PI-IN-20260409-001";
            }
            return null;
        }));

        Object reqVO = createSubmitReqVO(31L, Map.of("task_1", List.of(7L, 8L)));
        Method method = service.getClass().getMethod("submitPurchaseIn", Long.class, reqVO.getClass());

        Object result = method.invoke(service, 9527L, reqVO);

        // afterCommit 模式：submit 返回 null
        assertNull(result);
        assertEquals(31L, submitBizIdRef.get());
        // 第一次 updateById：设置 PROCESS 状态，processInstanceId=null
        assertEquals(31L, updatedPurchaseIns.get(0).getId());
        assertEquals(ErpAuditStatus.PROCESS.getStatus(), updatedPurchaseIns.get(0).getStatus());
        assertNull(invokeGetter(updatedPurchaseIns.get(0), "getProcessInstanceId"));
        // 第二次 updateById（afterCommit）：设置 processInstanceId
        assertEquals(31L, updatedPurchaseIns.get(1).getId());
        assertEquals("PI-IN-20260409-001", invokeGetter(updatedPurchaseIns.get(1), "getProcessInstanceId"));
    }

    @Test
    void cancelPurchaseInApproval_shouldCancelProcessAndClearBinding() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpPurchaseInDO> purchaseInRef = new AtomicReference<>(
                withProcessInstance(new ErpPurchaseInDO()
                        .setId(32L)
                        .setNo("PI-2026-CANCEL")
                        .setStatus(ErpAuditStatus.PROCESS.getStatus()), "PI-IN-TO-CANCEL")
        );
        AtomicReference<Long> clearedInIdRef = new AtomicReference<>();
        AtomicReference<Long> cancelBizIdRef = new AtomicReference<>();

        setField(service, "purchaseInMapper", createProxy(ErpPurchaseInMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return purchaseInRef.get();
            }
            if ("clearProcessInstanceId".equals(methodName)) {
                clearedInIdRef.set((Long) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "approvalRuntimeService", createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> {
            if ("cancel".equals(methodName)) {
                cancelBizIdRef.set((Long) args[1]);
            }
            return null;
        }));

        Object reqVO = createCancelReqVO(32L, "cancel test");
        Method method = service.getClass().getMethod("cancelPurchaseInApproval", Long.class, reqVO.getClass());
        method.invoke(service, 9527L, reqVO);

        // afterCommit 模式：BPM 撤回在 afterCommit 中执行
        assertEquals(32L, cancelBizIdRef.get());
        assertEquals(32L, clearedInIdRef.get());
    }

    @Test
    void handleProcessInstanceResult_shouldDoNothingAsHandledByResultHandler() throws Exception {
        Object service = instantiateService();

        Method method = service.getClass().getMethod("handleProcessInstanceResult",
                Long.class, String.class, Integer.class, String.class);
        // 结果回写已收敛到 PurchaseInResultHandler，此方法为空实现
        method.invoke(service, 33L, "PI-MATCH", bpmStatus("APPROVE"), "approved");
        // 无异常即通过
    }

    private Object instantiateService() throws Exception {
        Class<?> clazz = Class.forName("cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseInBpmServiceImpl");
        return clazz.getDeclaredConstructor().newInstance();
    }

    private Object createSubmitReqVO(Long id, Map<String, List<Long>> assignees) throws Exception {
        Class<?> clazz = Class.forName("cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInSubmitReqVO");
        Object reqVO = clazz.getDeclaredConstructor().newInstance();
        clazz.getMethod("setId", Long.class).invoke(reqVO, id);
        clazz.getMethod("setStartUserSelectAssignees", Map.class).invoke(reqVO, assignees);
        return reqVO;
    }

    private Object createCancelReqVO(Long id, String reason) throws Exception {
        Class<?> clazz = Class.forName("cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInCancelApprovalReqVO");
        Object reqVO = clazz.getDeclaredConstructor().newInstance();
        clazz.getMethod("setId", Long.class).invoke(reqVO, id);
        clazz.getMethod("setReason", String.class).invoke(reqVO, reason);
        return reqVO;
    }

    private Object createProxyByName(String className, MethodHandler handler) throws Exception {
        return createProxy(Class.forName(className), handler);
    }

    private Object readProperty(Object target, String methodName) throws Exception {
        return target.getClass().getMethod(methodName).invoke(target);
    }

    private Object invokeGetter(Object target, String methodName) throws Exception {
        return target.getClass().getMethod(methodName).invoke(target);
    }

    private ErpPurchaseInDO withProcessInstance(ErpPurchaseInDO purchaseIn, String processInstanceId) throws Exception {
        purchaseIn.getClass().getMethod("setProcessInstanceId", String.class).invoke(purchaseIn, processInstanceId);
        return purchaseIn;
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
        Object handle(String methodName, Object[] args) throws Exception;
    }

}
