package cn.weitee.erp.module.bpm.service.approval;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.weitee.erp.module.bpm.api.task.BpmProcessInstanceApi;
import cn.weitee.erp.module.bpm.controller.admin.approval.vo.scene.BpmApprovalSceneRespVO;
import cn.weitee.erp.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCancelReqVO;
import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalInstanceSnapshotDO;
import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalRuleDO;
import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalSchemeVersionDO;
import cn.weitee.erp.module.bpm.dal.mysql.approval.BpmApprovalRuleMapper;
import cn.weitee.erp.module.bpm.dal.mysql.approval.BpmApprovalSchemeVersionMapper;
import cn.weitee.erp.module.bpm.enums.approval.BpmApprovalInstanceSnapshotStatusEnum;
import cn.weitee.erp.module.bpm.enums.approval.BpmApprovalSceneStatusEnum;
import cn.weitee.erp.module.bpm.enums.approval.BpmApprovalSchemeStatusEnum;
import cn.weitee.erp.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.weitee.erp.module.bpm.framework.flowable.core.event.BpmProcessInstanceEventPublisher;
import cn.weitee.erp.module.bpm.service.task.BpmProcessInstanceService;
import cn.weitee.erp.module.bpm.service.approval.engine.RuleConditionEvaluator;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContext;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContextProvider;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static cn.weitee.erp.module.bpm.enums.ErrorCodeConstants.APPROVAL_INSTANCE_ALREADY_PROCESSING;
import static cn.weitee.erp.module.bpm.enums.ErrorCodeConstants.APPROVAL_INSTANCE_SNAPSHOT_NOT_EXISTS;
import static cn.weitee.erp.module.bpm.enums.ErrorCodeConstants.APPROVAL_PROCESS_INSTANCE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BpmApprovalRuntimeServiceImplTest {

    @Test
    void submit_shouldReturnRealProcessInstanceIdAndWriteBackSnapshot() throws Exception {
        BpmApprovalRuntimeServiceImpl service = new BpmApprovalRuntimeServiceImpl();
        AtomicReference<String> writtenProcessInstanceId = new AtomicReference<>();
        AtomicReference<Integer> writtenStatus = new AtomicReference<>();
        AtomicReference<String> writtenReason = new AtomicReference<>();
        AtomicReference<Map<String, Object>> submittedVariables = new AtomicReference<>();

        setField(service, "transactionManager", createTransactionManagerProxy());
        setField(service, "ruleConditionEvaluator", new RuleConditionEvaluator());
        setField(service, "approvalSceneService", createProxy(BpmApprovalSceneService.class, (methodName, args) -> {
            if ("getSceneByCode".equals(methodName)) {
                BpmApprovalSceneRespVO scene = new BpmApprovalSceneRespVO();
                scene.setSceneCode("erp.finance.payment.submit");
                scene.setStatus(BpmApprovalSceneStatusEnum.ENABLED.getStatus());
                scene.setActiveSchemeId(88L);
                return scene;
            }
            return null;
        }));
        setField(service, "approvalSchemeVersionMapper", createProxy(BpmApprovalSchemeVersionMapper.class, (methodName, args) -> {
            if ("selectBySchemeIdAndStatus".equals(methodName)) {
                return BpmApprovalSchemeVersionDO.builder()
                        .id(99L)
                        .schemeId(88L)
                        .status(BpmApprovalSchemeStatusEnum.ACTIVE.getStatus())
                        .build();
            }
            return null;
        }));
        setField(service, "approvalRuleMapper", createProxy(BpmApprovalRuleMapper.class, (methodName, args) -> {
            if ("selectListBySchemeVersionId".equals(methodName)) {
                return List.of(BpmApprovalRuleDO.builder()
                        .id(77L)
                        .ruleName("默认规则")
                        .priority(1)
                        .enabled(true)
                        .defaultRule(true)
                        .processJson("erp_finance_payment")
                        .build());
            }
            return null;
        }));
        setField(service, "approvalInstanceSnapshotService", createProxy(BpmApprovalInstanceSnapshotService.class, (methodName, args) -> {
            if ("getSnapshotBySceneCodeAndBizId".equals(methodName)) {
                return null;
            }
            if ("createSnapshot".equals(methodName)) {
                return 123L;
            }
            if ("updateSnapshotProcessInstanceId".equals(methodName)) {
                writtenProcessInstanceId.set((String) args[1]);
                return null;
            }
            if ("updateSnapshotStatus".equals(methodName)) {
                writtenStatus.set((Integer) args[1]);
                writtenReason.set((String) args[2]);
                return null;
            }
            return null;
        }));
        setField(service, "processInstanceApi", createProxy(BpmProcessInstanceApi.class, (methodName, args) -> {
            if ("createProcessInstance".equals(methodName)) {
                Object reqDTO = args[1];
                Map<String, Object> variables = (Map<String, Object>) readProperty(reqDTO, "getVariables");
                submittedVariables.set(variables);
                return "PROC-20260707-001";
            }
            return null;
        }));
        setField(service, "contextProviderMap", Map.of("erp.finance.payment.submit",
                createProxy(ApprovalContextProvider.class, (methodName, args) -> {
                    if ("getSceneCode".equals(methodName)) {
                        return "erp.finance.payment.submit";
                    }
                    if ("getContext".equals(methodName)) {
                        return ApprovalContext.builder()
                                .bizId(981502L)
                                .bizNo("PAY-FINENH-202605-002")
                                .variables(Map.of("amount", 88.66, "deptId", 11L))
                                .build();
                    }
                    return null;
                })));

        String processInstanceId = service.submit("erp.finance.payment.submit", 981502L, 9527L);

        assertEquals("PROC-20260707-001", processInstanceId);
        assertEquals("PROC-20260707-001", writtenProcessInstanceId.get());
        assertNull(writtenStatus.get(), "成功时不应把快照写成失败");
        assertNotNull(submittedVariables.get());
        assertEquals("erp.finance.payment.submit", submittedVariables.get().get("sceneCode"));
        assertEquals("981502", submittedVariables.get().get("bizId"));
        assertEquals(123L, submittedVariables.get().get("snapshotId"));
    }

    @Test
    void submit_shouldMarkSnapshotFailedAndThrowWhenProcessLaunchFails() throws Exception {
        BpmApprovalRuntimeServiceImpl service = new BpmApprovalRuntimeServiceImpl();
        AtomicReference<Integer> writtenStatus = new AtomicReference<>();
        AtomicReference<String> writtenReason = new AtomicReference<>();

        setField(service, "transactionManager", createTransactionManagerProxy());
        setField(service, "ruleConditionEvaluator", new RuleConditionEvaluator());
        setField(service, "approvalSceneService", createProxy(BpmApprovalSceneService.class, (methodName, args) -> {
            if ("getSceneByCode".equals(methodName)) {
                BpmApprovalSceneRespVO scene = new BpmApprovalSceneRespVO();
                scene.setSceneCode("erp.finance.expense.submit");
                scene.setStatus(BpmApprovalSceneStatusEnum.ENABLED.getStatus());
                scene.setActiveSchemeId(108L);
                return scene;
            }
            return null;
        }));
        setField(service, "approvalSchemeVersionMapper", createProxy(BpmApprovalSchemeVersionMapper.class, (methodName, args) -> {
            if ("selectBySchemeIdAndStatus".equals(methodName)) {
                return BpmApprovalSchemeVersionDO.builder()
                        .id(109L)
                        .schemeId(108L)
                        .status(BpmApprovalSchemeStatusEnum.ACTIVE.getStatus())
                        .build();
            }
            return null;
        }));
        setField(service, "approvalRuleMapper", createProxy(BpmApprovalRuleMapper.class, (methodName, args) -> {
            if ("selectListBySchemeVersionId".equals(methodName)) {
                return List.of(BpmApprovalRuleDO.builder()
                        .id(110L)
                        .ruleName("默认规则")
                        .priority(1)
                        .enabled(true)
                        .defaultRule(true)
                        .processJson("erp_finance_expense")
                        .build());
            }
            return null;
        }));
        setField(service, "approvalInstanceSnapshotService", createProxy(BpmApprovalInstanceSnapshotService.class, (methodName, args) -> {
            if ("getSnapshotBySceneCodeAndBizId".equals(methodName)) {
                return null;
            }
            if ("createSnapshot".equals(methodName)) {
                return 456L;
            }
            if ("updateSnapshotStatus".equals(methodName)) {
                writtenStatus.set((Integer) args[1]);
                writtenReason.set((String) args[2]);
                return null;
            }
            return null;
        }));
        setField(service, "processInstanceApi", createProxy(BpmProcessInstanceApi.class, (methodName, args) -> {
            if ("createProcessInstance".equals(methodName)) {
                throw new IllegalStateException("flowable unavailable");
            }
            return null;
        }));
        setField(service, "contextProviderMap", Map.of("erp.finance.expense.submit",
                createProxy(ApprovalContextProvider.class, (methodName, args) -> {
                    if ("getSceneCode".equals(methodName)) {
                        return "erp.finance.expense.submit";
                    }
                    if ("getContext".equals(methodName)) {
                        return ApprovalContext.builder()
                                .bizId(981704L)
                                .bizNo("EXP-FINENH-202605-004")
                                .variables(Map.of("amount", 199.99))
                                .build();
                    }
                    return null;
                })));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.submit("erp.finance.expense.submit", 981704L, 9527L));

        assertEquals("flowable unavailable", ex.getMessage());
        assertEquals(BpmApprovalInstanceSnapshotStatusEnum.FAILED.getStatus(), writtenStatus.get());
        assertNotNull(writtenReason.get());
    }

    @Test
    void matchRule_shouldSupportImmutableRuleList() throws Exception {
        BpmApprovalRuntimeServiceImpl service = new BpmApprovalRuntimeServiceImpl();
        setField(service, "ruleConditionEvaluator", new RuleConditionEvaluator());

        List<BpmApprovalRuleDO> rules = List.of(
                BpmApprovalRuleDO.builder()
                        .id(2L)
                        .ruleName("默认规则")
                        .priority(2)
                        .enabled(true)
                        .defaultRule(true)
                        .processJson("erp_finance_payment")
                        .build(),
                BpmApprovalRuleDO.builder()
                        .id(1L)
                        .ruleName("高优先级规则")
                        .priority(1)
                        .enabled(true)
                        .defaultRule(false)
                        .conditionJson("")
                        .processJson("erp_finance_payment")
                        .build()
        );

        ApprovalContext context = ApprovalContext.builder()
                .bizId(1L)
                .bizNo("PAY-TEST-001")
                .build();

        BpmApprovalRuleDO hitRule = invokeMatchRule(service, rules, context);

        assertNotNull(hitRule);
        assertEquals(1L, hitRule.getId());
    }

    @Test
    void submit_shouldRejectWhenExistingProcessingSnapshotPresent() throws Exception {
        BpmApprovalRuntimeServiceImpl service = new BpmApprovalRuntimeServiceImpl();
        setField(service, "transactionManager", createTransactionManagerProxy());
        setField(service, "approvalSceneService", createProxy(BpmApprovalSceneService.class, (methodName, args) -> {
            if ("getSceneByCode".equals(methodName)) {
                BpmApprovalSceneRespVO scene = new BpmApprovalSceneRespVO();
                scene.setSceneCode("erp.finance.payment.submit");
                scene.setStatus(BpmApprovalSceneStatusEnum.ENABLED.getStatus());
                scene.setActiveSchemeId(88L);
                return scene;
            }
            return null;
        }));
        setField(service, "approvalInstanceSnapshotService", createProxy(BpmApprovalInstanceSnapshotService.class, (methodName, args) -> {
            if ("getSnapshotBySceneCodeAndBizId".equals(methodName)) {
                return BpmApprovalInstanceSnapshotDO.builder()
                        .id(12L)
                        .sceneCode("erp.finance.payment.submit")
                        .bizId("981502")
                        .status(BpmApprovalInstanceSnapshotStatusEnum.PROCESSING.getStatus())
                        .build();
            }
            return null;
        }));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.submit("erp.finance.payment.submit", 981502L, 9527L));

        assertEquals(APPROVAL_INSTANCE_ALREADY_PROCESSING.getCode(), ex.getCode());
    }

    @Test
    void submit_shouldDeleteFinishedSnapshotBeforeReSubmit() throws Exception {
        BpmApprovalRuntimeServiceImpl service = new BpmApprovalRuntimeServiceImpl();
        AtomicReference<Long> deletedSnapshotIdRef = new AtomicReference<>();
        AtomicReference<Long> createdSnapshotIdRef = new AtomicReference<>();

        setField(service, "transactionManager", createTransactionManagerProxy());
        setField(service, "ruleConditionEvaluator", new RuleConditionEvaluator());
        setField(service, "approvalSceneService", createProxy(BpmApprovalSceneService.class, (methodName, args) -> {
            if ("getSceneByCode".equals(methodName)) {
                BpmApprovalSceneRespVO scene = new BpmApprovalSceneRespVO();
                scene.setSceneCode("erp.finance.payment.submit");
                scene.setStatus(BpmApprovalSceneStatusEnum.ENABLED.getStatus());
                scene.setActiveSchemeId(88L);
                return scene;
            }
            return null;
        }));
        setField(service, "approvalSchemeVersionMapper", createProxy(BpmApprovalSchemeVersionMapper.class, (methodName, args) -> {
            if ("selectBySchemeIdAndStatus".equals(methodName)) {
                return BpmApprovalSchemeVersionDO.builder()
                        .id(99L)
                        .schemeId(88L)
                        .status(BpmApprovalSchemeStatusEnum.ACTIVE.getStatus())
                        .build();
            }
            return null;
        }));
        setField(service, "approvalRuleMapper", createProxy(BpmApprovalRuleMapper.class, (methodName, args) -> {
            if ("selectListBySchemeVersionId".equals(methodName)) {
                return List.of(BpmApprovalRuleDO.builder()
                        .id(77L)
                        .ruleName("默认规则")
                        .priority(1)
                        .enabled(true)
                        .defaultRule(true)
                        .processJson("erp_finance_payment")
                        .build());
            }
            return null;
        }));
        setField(service, "approvalInstanceSnapshotService", createProxy(BpmApprovalInstanceSnapshotService.class, (methodName, args) -> {
            if ("getSnapshotBySceneCodeAndBizId".equals(methodName)) {
                return BpmApprovalInstanceSnapshotDO.builder()
                        .id(16L)
                        .sceneCode("erp.finance.payment.submit")
                        .bizId("981506")
                        .status(BpmApprovalInstanceSnapshotStatusEnum.FAILED.getStatus())
                        .build();
            }
            if ("deleteSnapshot".equals(methodName)) {
                deletedSnapshotIdRef.set((Long) args[0]);
                return null;
            }
            if ("createSnapshot".equals(methodName)) {
                BpmApprovalInstanceSnapshotDO snapshot = (BpmApprovalInstanceSnapshotDO) args[0];
                createdSnapshotIdRef.set(snapshot.getId());
                return 123L;
            }
            if ("updateSnapshotProcessInstanceId".equals(methodName)) {
                return null;
            }
            return null;
        }));
        setField(service, "processInstanceApi", createProxy(BpmProcessInstanceApi.class, (methodName, args) -> {
            if ("createProcessInstance".equals(methodName)) {
                return "PROC-RESUBMIT-001";
            }
            return null;
        }));
        setField(service, "contextProviderMap", Map.of("erp.finance.payment.submit",
                createProxy(ApprovalContextProvider.class, (methodName, args) -> {
                    if ("getSceneCode".equals(methodName)) {
                        return "erp.finance.payment.submit";
                    }
                    if ("getContext".equals(methodName)) {
                        return ApprovalContext.builder()
                                .bizId(981506L)
                                .bizNo("PAY-RETRY-001")
                                .variables(Map.of("amount", 66.00))
                                .build();
                    }
                    return null;
                })));

        String processInstanceId = service.submit("erp.finance.payment.submit", 981506L, 9527L);

        assertEquals("PROC-RESUBMIT-001", processInstanceId);
        assertEquals(16L, deletedSnapshotIdRef.get());
        assertNull(createdSnapshotIdRef.get());
    }

    @Test
    void getApprovalDetail_shouldRejectCancelledSnapshot() throws Exception {
        BpmApprovalRuntimeServiceImpl service = new BpmApprovalRuntimeServiceImpl();
        setField(service, "approvalInstanceSnapshotService", createProxy(BpmApprovalInstanceSnapshotService.class, (methodName, args) -> {
            if ("getEffectiveSnapshotBySceneCodeAndBizId".equals(methodName)) {
                return null;
            }
            return null;
        }));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.getApprovalDetail("erp.finance.payment.submit", 990001L));

        assertEquals(APPROVAL_INSTANCE_SNAPSHOT_NOT_EXISTS.getCode(), ex.getCode());
    }

    @Test
    void getApprovalDetail_shouldRejectSnapshotWithoutProcessInstanceId() throws Exception {
        BpmApprovalRuntimeServiceImpl service = new BpmApprovalRuntimeServiceImpl();
        setField(service, "approvalInstanceSnapshotService", createProxy(BpmApprovalInstanceSnapshotService.class, (methodName, args) -> {
            if ("getEffectiveSnapshotBySceneCodeAndBizId".equals(methodName)) {
                return BpmApprovalInstanceSnapshotDO.builder()
                        .id(12L)
                        .sceneCode("erp.finance.payment.submit")
                        .bizId("981502")
                        .status(BpmApprovalInstanceSnapshotStatusEnum.PROCESSING.getStatus())
                        .processInstanceId(null)
                        .build();
            }
            return null;
        }));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.getApprovalDetail("erp.finance.payment.submit", 981502L));

        assertEquals(APPROVAL_PROCESS_INSTANCE_NOT_EXISTS.getCode(), ex.getCode());
    }

    @Test
    void cancel_shouldDispatchCancelResultAfterProcessCancelSucceeds() throws Exception {
        BpmApprovalRuntimeServiceImpl service = new BpmApprovalRuntimeServiceImpl();
        AtomicReference<String> cancelledProcessInstanceId = new AtomicReference<>();
        AtomicReference<String> cancelReason = new AtomicReference<>();
        AtomicReference<BpmProcessInstanceStatusEvent> dispatchedEvent = new AtomicReference<>();

        setField(service, "transactionManager", createTransactionManagerProxy());
        setField(service, "approvalInstanceSnapshotService", createProxy(BpmApprovalInstanceSnapshotService.class, (methodName, args) -> {
            if ("getSnapshotBySceneCodeAndBizId".equals(methodName)) {
                return BpmApprovalInstanceSnapshotDO.builder()
                        .id(19L)
                        .sceneCode("erp.stock.in.submit")
                        .bizId("16")
                        .status(BpmApprovalInstanceSnapshotStatusEnum.PROCESSING.getStatus())
                        .processDefinitionKey("erp_stock_in")
                        .processInstanceId("PROC-STOCK-IN-16")
                        .build();
            }
            return null;
        }));
        setField(service, "approvalRecordService", createProxy(BpmApprovalRecordService.class, (methodName, args) -> null));
        setField(service, "processInstanceService", createProxy(BpmProcessInstanceService.class, (methodName, args) -> {
            if ("cancelProcessInstanceByStartUser".equals(methodName)) {
                BpmProcessInstanceCancelReqVO reqVO = (BpmProcessInstanceCancelReqVO) args[1];
                cancelledProcessInstanceId.set(reqVO.getId());
                cancelReason.set(reqVO.getReason());
                return null;
            }
            return null;
        }));
        setField(service, "processInstanceEventPublisher",
                new BpmProcessInstanceEventPublisher(event -> dispatchedEvent.set((BpmProcessInstanceStatusEvent) event)));

        service.cancel("erp.stock.in.submit", 16L, 145L, "撤回测试");

        assertEquals("PROC-STOCK-IN-16", cancelledProcessInstanceId.get());
        assertEquals("撤回测试", cancelReason.get());
        assertNotNull(dispatchedEvent.get());
        assertEquals("PROC-STOCK-IN-16", dispatchedEvent.get().getId());
        assertEquals("erp_stock_in", dispatchedEvent.get().getProcessDefinitionKey());
        assertEquals("16", dispatchedEvent.get().getBusinessKey());
        assertEquals(BpmProcessInstanceStatusEnum.CANCEL.getStatus(), dispatchedEvent.get().getStatus());
        assertEquals("撤回测试", dispatchedEvent.get().getReason());
    }

    @Test
    void cancel_shouldRecoverWhenFlowableAlreadyCanceledButSnapshotFailed() throws Exception {
        BpmApprovalRuntimeServiceImpl service = new BpmApprovalRuntimeServiceImpl();
        AtomicReference<String> cancelledProcessInstanceId = new AtomicReference<>();
        AtomicReference<BpmProcessInstanceStatusEvent> dispatchedEvent = new AtomicReference<>();

        setField(service, "transactionManager", createTransactionManagerProxy());
        setField(service, "approvalInstanceSnapshotService", createProxy(BpmApprovalInstanceSnapshotService.class, (methodName, args) -> {
            if ("getSnapshotBySceneCodeAndBizId".equals(methodName)) {
                return BpmApprovalInstanceSnapshotDO.builder()
                        .id(29L)
                        .sceneCode("erp.stock.in.submit")
                        .bizId("16")
                        .status(BpmApprovalInstanceSnapshotStatusEnum.FAILED.getStatus())
                        .processDefinitionKey("erp_stock_in")
                        .processInstanceId("PROC-STOCK-IN-16")
                        .resultReason("BPM撤回失败: sendProcessInstanceResultEvent.event.processDefinitionKey: 流程实例的 key 不能为空")
                        .build();
            }
            return null;
        }));
        setField(service, "approvalRecordService", createProxy(BpmApprovalRecordService.class, (methodName, args) -> null));
        setField(service, "processInstanceService", createProxy(BpmProcessInstanceService.class, (methodName, args) -> {
            if ("isHistoricProcessInstanceCanceled".equals(methodName)) {
                return true;
            }
            if ("cancelProcessInstanceByStartUser".equals(methodName)) {
                BpmProcessInstanceCancelReqVO reqVO = (BpmProcessInstanceCancelReqVO) args[1];
                cancelledProcessInstanceId.set(reqVO.getId());
            }
            return null;
        }));
        setField(service, "processInstanceEventPublisher",
                new BpmProcessInstanceEventPublisher(event -> dispatchedEvent.set((BpmProcessInstanceStatusEvent) event)));

        service.cancel("erp.stock.in.submit", 16L, 145L, "补发撤回事件");

        assertNull(cancelledProcessInstanceId.get(), "历史流程已撤回结束时不应再次调用 Flowable 撤回");
        assertNotNull(dispatchedEvent.get());
        assertEquals("PROC-STOCK-IN-16", dispatchedEvent.get().getId());
        assertEquals("erp_stock_in", dispatchedEvent.get().getProcessDefinitionKey());
        assertEquals("16", dispatchedEvent.get().getBusinessKey());
        assertEquals(BpmProcessInstanceStatusEnum.CANCEL.getStatus(), dispatchedEvent.get().getStatus());
        assertEquals("补发撤回事件", dispatchedEvent.get().getReason());
    }

    private BpmApprovalRuleDO invokeMatchRule(BpmApprovalRuntimeServiceImpl target,
                                              List<BpmApprovalRuleDO> rules,
                                              ApprovalContext context) throws Exception {
        Method method = BpmApprovalRuntimeServiceImpl.class.getDeclaredMethod(
                "matchRule", List.class, ApprovalContext.class);
        method.setAccessible(true);
        return (BpmApprovalRuleDO) method.invoke(target, rules, context);
    }

    private Object readProperty(Object target, String methodName) throws Exception {
        return target.getClass().getMethod(methodName).invoke(target);
    }

    private PlatformTransactionManager createTransactionManagerProxy() {
        return createProxy(PlatformTransactionManager.class, (methodName, args) -> {
            if ("getTransaction".equals(methodName)) {
                return new SimpleTransactionStatus();
            }
            if ("commit".equals(methodName) || "rollback".equals(methodName)) {
                return null;
            }
            return null;
        });
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
