package cn.weitee.erp.module.bpm.service.approval;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.common.util.collection.CollectionUtils;
import cn.weitee.erp.framework.common.util.number.NumberUtils;
import cn.weitee.erp.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.weitee.erp.module.bpm.api.task.BpmProcessInstanceApi;
import cn.weitee.erp.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.weitee.erp.module.bpm.controller.admin.approval.vo.scene.BpmApprovalSceneRespVO;
import cn.weitee.erp.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailReqVO;
import cn.weitee.erp.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailRespVO;
import cn.weitee.erp.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCancelReqVO;
import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalInstanceSnapshotDO;
import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalRecordDO;
import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalRuleDO;
import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalSchemeDO;
import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalSchemeVersionDO;
import cn.weitee.erp.module.bpm.dal.mysql.approval.BpmApprovalRuleMapper;
import cn.weitee.erp.module.bpm.dal.mysql.approval.BpmApprovalSchemeVersionMapper;
import cn.weitee.erp.module.bpm.enums.approval.BpmApprovalInstanceSnapshotStatusEnum;
import cn.weitee.erp.module.bpm.enums.approval.BpmApprovalSceneStatusEnum;
import cn.weitee.erp.module.bpm.enums.approval.BpmApprovalSchemeStatusEnum;
import cn.weitee.erp.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.weitee.erp.module.bpm.framework.flowable.core.event.BpmProcessInstanceEventPublisher;
import cn.weitee.erp.module.bpm.service.approval.handler.ApprovalResultHandler;
import cn.weitee.erp.module.bpm.service.approval.generic.GenericApprovalConfigService;
import cn.weitee.erp.module.bpm.service.approval.generic.GenericApprovalContextProvider;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContext;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContextProvider;
import cn.weitee.erp.module.bpm.service.approval.engine.RuleConditionEvaluator;
import cn.weitee.erp.module.bpm.service.task.BpmProcessInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.bpm.enums.ErrorCodeConstants.*;

/**
 * 统一审批运行时门面实现类
 */
@Service
@Validated
@Slf4j
public class BpmApprovalRuntimeServiceImpl implements BpmApprovalRuntimeService {

    @Resource
    private BpmApprovalSceneService approvalSceneService;
    @Resource
    private BpmApprovalSchemeVersionMapper approvalSchemeVersionMapper;
    @Resource
    private BpmApprovalRuleMapper approvalRuleMapper;
    @Resource
    private BpmApprovalInstanceSnapshotService approvalInstanceSnapshotService;
    @Resource
    private BpmApprovalRecordService approvalRecordService;

    @Resource
    private BpmProcessInstanceApi processInstanceApi;
    @Resource
    private BpmProcessInstanceService processInstanceService;
    @Resource
    private BpmProcessInstanceEventPublisher processInstanceEventPublisher;
    @Resource
    private BpmApprovalEventDispatcher approvalEventDispatcher;
    @Resource
    private RuleConditionEvaluator ruleConditionEvaluator;
    @Resource
    private PlatformTransactionManager transactionManager;

    private Map<String, ApprovalContextProvider> contextProviderMap;
    private Map<String, ApprovalResultHandler> resultHandlerMap;

    @Resource
    private GenericApprovalConfigService genericConfigService;

    @Resource
    private GenericApprovalContextProvider genericApprovalContextProvider;

    /**
     * 注入所有 ApprovalContextProvider
     */
    @Resource
    public void setContextProviders(List<ApprovalContextProvider> providers) {
        this.contextProviderMap = CollectionUtils.convertMap(providers, ApprovalContextProvider::getSceneCode);
    }

    /**
     * 注入所有 ApprovalResultHandler
     */
    @Resource
    public void setResultHandlers(List<ApprovalResultHandler> handlers) {
        this.resultHandlerMap = CollectionUtils.convertMap(handlers, ApprovalResultHandler::getSceneCode);
    }

    @Override
    public String submit(String sceneCode, Long bizId, Long userId) {
        SubmitPreparation preparation = executeInRequiredTransaction(() ->
                prepareSubmitInTransaction(sceneCode, bizId, userId));
        return launchProcess(preparation);
    }

    SubmitPreparation prepareSubmitInTransaction(String sceneCode, Long bizId, Long userId) {
        // 1. 校验场景存在（getSceneByCode 已内置不存在时抛异常）
        BpmApprovalSceneRespVO scene = approvalSceneService.getSceneByCode(sceneCode);
        if (!ObjectUtil.equal(scene.getStatus(), BpmApprovalSceneStatusEnum.ENABLED.getStatus())) {
            throw exception(APPROVAL_SCENE_DISABLED);
        }

        // 2. 防重检查：同一业务单据在同一场景下不允许重复提交
        //    铁律 5：区分"已完成的终态"和"handler 失败的伪终态" — FAILED 状态允许重新提交
        BpmApprovalInstanceSnapshotDO existingSnapshot = approvalInstanceSnapshotService
                .getSnapshotBySceneCodeAndBizId(sceneCode, String.valueOf(bizId));
        if (existingSnapshot != null) {
            if (ObjectUtil.equal(existingSnapshot.getStatus(),
                    BpmApprovalInstanceSnapshotStatusEnum.PROCESSING.getStatus())) {
                // 审批中，拒绝重复提交
                throw exception(APPROVAL_INSTANCE_ALREADY_PROCESSING, sceneCode, bizId);
            }
            if (ObjectUtil.equal(existingSnapshot.getStatus(),
                    BpmApprovalInstanceSnapshotStatusEnum.FAILED.getStatus())) {
                // 失败状态，清理旧快照后允许重新提交
                log.info("[submit] 检测到FAILED状态快照，允许重新提交，sceneCode={}, bizId={}", sceneCode, bizId);
            }
            // 已完结（通过/驳回/撤回/失败），清理旧快照后允许重新提交
            approvalInstanceSnapshotService.deleteSnapshot(existingSnapshot.getId());
        }

        // 3. 获取上下文提供者（精确匹配；未命中时兜底通用审批接入）
        ApprovalContextProvider contextProvider = contextProviderMap.get(sceneCode);
        if (contextProvider == null && genericConfigService.isGenericEnabled(sceneCode)) {
            contextProvider = genericApprovalContextProvider;
        }
        if (contextProvider == null) {
            throw exception(APPROVAL_CONTEXT_PROVIDER_NOT_FOUND, sceneCode);
        }

        // 4. 获取业务上下文
        ApprovalContext context;
        if (contextProvider instanceof GenericApprovalContextProvider genericProvider) {
            context = genericProvider.getContext(sceneCode, bizId);
        } else {
            context = contextProvider.getContext(bizId);
        }
        if (context == null) {
            throw exception(APPROVAL_CONTEXT_IS_NULL, sceneCode, bizId);
        }

        // 5. 获取当前正式审批方案
        Long activeSchemeId = scene.getActiveSchemeId();
        if (activeSchemeId == null) {
            throw exception(APPROVAL_SCHEME_NOT_ACTIVE, sceneCode);
        }
        BpmApprovalSchemeVersionDO activeVersion = approvalSchemeVersionMapper.selectBySchemeIdAndStatus(
                activeSchemeId, BpmApprovalSchemeStatusEnum.ACTIVE.getStatus());
        if (activeVersion == null) {
            throw exception(APPROVAL_SCHEME_VERSION_NOT_ACTIVE, activeSchemeId);
        }

        // 6. 命中规则（优先匹配条件规则，无匹配时回退到默认规则）
        List<BpmApprovalRuleDO> rules = approvalRuleMapper.selectListBySchemeVersionId(activeVersion.getId());
        BpmApprovalRuleDO hitRule = matchRule(rules, context);
        if (hitRule == null) {
            throw exception(APPROVAL_RULE_NOT_FOUND, activeVersion.getId());
        }

        // 7. 创建运行时快照
        // processDefinitionKey：BPM 启动所需流程定义 Key，取自规则的 processJson 字段
        // processJson：快照留存的流程配置 JSON，当前仅存 Key；未来扩展节点配置时在此追加
        String processKey = hitRule.getProcessJson();
        String approvalId = java.util.UUID.randomUUID().toString();
        BpmApprovalInstanceSnapshotDO snapshot = BpmApprovalInstanceSnapshotDO.builder()
                .approvalId(approvalId)
                .sceneCode(sceneCode)
                .bizId(String.valueOf(bizId))
                .startUserId(userId)
                .schemeId(activeSchemeId)
                .schemeVersionId(activeVersion.getId())
                .ruleId(hitRule.getId())
                .processDefinitionKey(processKey)
                .processJson(null)
                .notifyJson(parseNotifyJson(activeVersion.getNotifyJson()))
                .contextJson(context.getVariables())
                .status(BpmApprovalInstanceSnapshotStatusEnum.PROCESSING.getStatus())
                .build();
        Long snapshotId = approvalInstanceSnapshotService.createSnapshot(snapshot);

        // 8. 生成 BPM 启动参数
        Map<String, Object> variables = new HashMap<>(context.getVariables());
        variables.put("sceneCode", sceneCode);
        variables.put("bizId", String.valueOf(bizId));
        variables.put("snapshotId", snapshotId);

        return new SubmitPreparation(snapshotId, sceneCode, bizId, userId, processKey, variables);
    }

    @Override
    public BpmApprovalDetailRespVO getApprovalDetail(String sceneCode, Long bizId) {
        BpmApprovalInstanceSnapshotDO snapshot = getSnapshotForApprovalDetail(sceneCode, bizId);
        BpmApprovalDetailReqVO reqVO = new BpmApprovalDetailReqVO();
        reqVO.setProcessInstanceId(snapshot.getProcessInstanceId());
        return processInstanceService.getApprovalDetail(NumberUtils.parseLong(snapshot.getCreator()), reqVO);
    }

    @Override
    public List<BpmApprovalDetailRespVO.ActivityNode> getApprovalTrail(String sceneCode, Long bizId) {
        BpmApprovalDetailRespVO detail = getApprovalDetail(sceneCode, bizId);
        return detail == null ? null : detail.getActivityNodes();
    }

    @Override
    public void cancel(String sceneCode, Long bizId, Long userId, String reason) {
        CancelPreparation preparation = executeInRequiredTransaction(() ->
                prepareCancelInTransaction(sceneCode, bizId, userId, reason));
        cancelProcess(preparation);
    }

    CancelPreparation prepareCancelInTransaction(String sceneCode, Long bizId, Long userId, String reason) {
        // 1. 查找快照
        BpmApprovalInstanceSnapshotDO snapshot = approvalInstanceSnapshotService
                .getSnapshotBySceneCodeAndBizId(sceneCode, String.valueOf(bizId));
        if (snapshot == null) {
            throw exception(APPROVAL_INSTANCE_SNAPSHOT_NOT_EXISTS);
        }
        boolean recoverCanceledProcess = isRecoverableCanceledProcess(snapshot);
        // 撤回失败（FAILED）允许重试：与提交路径的 FAILED 重试口径对齐，
        // 否则撤回一旦失败快照即死锁，业务单据将永久卡在审批中状态
        boolean retryableFailedCancel = ObjectUtil.equal(snapshot.getStatus(),
                BpmApprovalInstanceSnapshotStatusEnum.FAILED.getStatus())
                && StrUtil.isNotBlank(snapshot.getProcessInstanceId());
        if (!ObjectUtil.equal(snapshot.getStatus(), BpmApprovalInstanceSnapshotStatusEnum.PROCESSING.getStatus())
                && !recoverCanceledProcess && !retryableFailedCancel) {
            throw exception(APPROVAL_INSTANCE_NOT_PROCESSING);
        }

        // 2. 校验流程实例存在
        if (StrUtil.isBlank(snapshot.getProcessInstanceId())) {
            throw exception(APPROVAL_PROCESS_INSTANCE_NOT_EXISTS);
        }

        // 3. 记录审批操作（本地事务内）
        BpmApprovalRecordDO record = BpmApprovalRecordDO.builder()
                .approvalId(snapshot.getApprovalId())
                .action("CANCEL")
                .operatorUserId(userId)
                .comment(reason)
                .build();
        approvalRecordService.createRecord(record);

        String processDefinitionKey = StrUtil.blankToDefault(snapshot.getProcessDefinitionKey(), sceneCode);
        return new CancelPreparation(snapshot.getId(), sceneCode, bizId, userId,
                snapshot.getProcessInstanceId(), processDefinitionKey, reason, recoverCanceledProcess);
    }

    private String launchProcess(SubmitPreparation preparation) {
        try {
            String processInstanceId = processInstanceApi.createProcessInstance(preparation.userId(),
                    new BpmProcessInstanceCreateReqDTO()
                            .setProcessDefinitionKey(preparation.processDefinitionKey())
                            .setBusinessKey(String.valueOf(preparation.bizId()))
                            .setVariables(preparation.variables()));
            executeInRequiredTransaction(() ->
                    approvalInstanceSnapshotService.updateSnapshotProcessInstanceId(preparation.snapshotId(), processInstanceId));
            return processInstanceId;
        } catch (Exception e) {
            log.error("[submit] BPM 创建失败，snapshotId={}, sceneCode={}, bizId={}",
                    preparation.snapshotId(), preparation.sceneCode(), preparation.bizId(), e);
            try {
                executeInRequiredTransaction(() ->
                        approvalInstanceSnapshotService.updateSnapshotStatus(preparation.snapshotId(),
                                BpmApprovalInstanceSnapshotStatusEnum.FAILED.getStatus(),
                                "BPM创建失败: " + e.getMessage()));
            } catch (Exception ex) {
                log.error("[submit] 更新快照为FAILED状态也失败，snapshotId={}", preparation.snapshotId(), ex);
            }
            throw e;
        }
    }

    private void cancelProcess(CancelPreparation preparation) {
        try {
            if (!preparation.flowableAlreadyCanceled()) {
                processInstanceService.cancelProcessInstanceByStartUser(preparation.userId(),
                        new BpmProcessInstanceCancelReqVO()
                                .setId(preparation.processInstanceId())
                                .setReason(preparation.reason()));
            }
            processInstanceEventPublisher.sendProcessInstanceResultEvent(new BpmProcessInstanceStatusEvent(this)
                    .setId(preparation.processInstanceId())
                    .setProcessDefinitionKey(preparation.processDefinitionKey())
                    .setStatus(BpmProcessInstanceStatusEnum.CANCEL.getStatus())
                    .setBusinessKey(String.valueOf(preparation.bizId()))
                    .setReason(preparation.reason()));
        } catch (Exception e) {
            log.error("[cancel] BPM 撤回失败，sceneCode={}, bizId={}, processInstanceId={}",
                    preparation.sceneCode(), preparation.bizId(), preparation.processInstanceId(), e);
            try {
                executeInRequiredTransaction(() ->
                        approvalInstanceSnapshotService.updateSnapshotStatus(preparation.snapshotId(),
                                BpmApprovalInstanceSnapshotStatusEnum.FAILED.getStatus(),
                                "BPM撤回失败: " + e.getMessage()));
            } catch (Exception ex) {
                log.error("[cancel] 更新快照为FAILED状态也失败，snapshotId={}", preparation.snapshotId(), ex);
            }
            throw e;
        }
    }

    private boolean isRecoverableCanceledProcess(BpmApprovalInstanceSnapshotDO snapshot) {
        return ObjectUtil.equal(snapshot.getStatus(), BpmApprovalInstanceSnapshotStatusEnum.FAILED.getStatus())
                && StrUtil.isNotBlank(snapshot.getProcessInstanceId())
                && StrUtil.startWith(snapshot.getResultReason(), "BPM撤回失败:")
                && processInstanceService.isHistoricProcessInstanceCanceled(snapshot.getProcessInstanceId());
    }

    private <T> T executeInRequiredTransaction(java.util.function.Supplier<T> supplier) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return transactionTemplate.execute(status -> supplier.get());
    }

    private void executeInRequiredTransaction(Runnable runnable) {
        executeInRequiredTransaction(() -> {
            runnable.run();
            return null;
        });
    }

    @Override
    public void dispatchResult(BpmProcessInstanceStatusEvent event) {
        approvalEventDispatcher.onApplicationEvent(event);
    }

    private BpmApprovalInstanceSnapshotDO getSnapshotForApprovalDetail(String sceneCode, Long bizId) {
        BpmApprovalInstanceSnapshotDO snapshot = approvalInstanceSnapshotService
                .getEffectiveSnapshotBySceneCodeAndBizId(sceneCode, String.valueOf(bizId));
        if (snapshot == null) {
            throw exception(APPROVAL_INSTANCE_SNAPSHOT_NOT_EXISTS);
        }
        if (StrUtil.isBlank(snapshot.getProcessInstanceId())) {
            throw exception(APPROVAL_PROCESS_INSTANCE_NOT_EXISTS);
        }
        return snapshot;
    }

    /**
     * 匹配审批规则
     *
     * 匹配策略：
     * 1. 按优先级从高到低排序
     * 2. 优先匹配有条件表达式的规则（conditionJson 不为空）
     * 3. 条件匹配成功则返回该规则
     * 4. 无条件匹配时，回退到默认规则（defaultRule = true）
     *
     * @param rules 规则列表
     * @param context 审批上下文
     * @return 命中的规则，无匹配时返回 null
     */
    private BpmApprovalRuleDO matchRule(List<BpmApprovalRuleDO> rules, ApprovalContext context) {
        // 1. 过滤启用的规则，按优先级排序（priority 越小越优先）
        List<BpmApprovalRuleDO> enabledRules = rules.stream()
                .filter(rule -> Boolean.TRUE.equals(rule.getEnabled()))
                .sorted(Comparator.comparingInt(rule -> rule.getPriority() != null ? rule.getPriority() : Integer.MAX_VALUE))
                .collect(Collectors.toList());
        if (enabledRules.isEmpty()) {
            return null;
        }

        // 2. 优先匹配条件规则
        for (BpmApprovalRuleDO rule : enabledRules) {
            if (Boolean.TRUE.equals(rule.getDefaultRule())) {
                continue; // 默认规则稍后处理
            }
            if (ruleConditionEvaluator.evaluate(rule.getConditionJson(), context)) {
                log.info("[matchRule] 命中条件规则：ruleId={}, ruleName={}", rule.getId(), rule.getRuleName());
                return rule;
            }
        }

        // 4. 回退到默认规则
        return enabledRules.stream()
                .filter(rule -> Boolean.TRUE.equals(rule.getDefaultRule()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 解析通知配置 JSON 字符串为 Map
     *
     * @param notifyJson 通知配置 JSON 字符串
     * @return 解析后的 Map，若输入为空或解析失败则返回 null
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseNotifyJson(String notifyJson) {
        if (notifyJson == null || notifyJson.isEmpty()) {
            return null;
        }
        try {
            return cn.hutool.json.JSONUtil.toBean(notifyJson, Map.class);
        } catch (Exception e) {
            log.warn("[parseNotifyJson] 解析通知配置 JSON 失败，原始值: {}", notifyJson, e);
            return null;
        }
    }

    private record SubmitPreparation(Long snapshotId, String sceneCode, Long bizId, Long userId,
                                     String processDefinitionKey, Map<String, Object> variables) {
    }

    private record CancelPreparation(Long snapshotId, String sceneCode, Long bizId, Long userId,
                                     String processInstanceId, String processDefinitionKey, String reason,
                                     boolean flowableAlreadyCanceled) {
    }

}
