package cn.iocoder.yudao.module.bpm.service.approval;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.scene.BpmApprovalSceneRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCancelReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalInstanceSnapshotDO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalRuleDO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalSchemeDO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalSchemeVersionDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.approval.BpmApprovalRuleMapper;
import cn.iocoder.yudao.module.bpm.dal.mysql.approval.BpmApprovalSchemeVersionMapper;
import cn.iocoder.yudao.module.bpm.enums.approval.BpmApprovalInstanceSnapshotStatusEnum;
import cn.iocoder.yudao.module.bpm.enums.approval.BpmApprovalSceneStatusEnum;
import cn.iocoder.yudao.module.bpm.enums.approval.BpmApprovalSchemeStatusEnum;
import cn.iocoder.yudao.module.bpm.service.approval.handler.ApprovalResultHandler;
import cn.iocoder.yudao.module.bpm.service.approval.provider.ApprovalContext;
import cn.iocoder.yudao.module.bpm.service.approval.provider.ApprovalContextProvider;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.*;

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
    private BpmProcessInstanceApi processInstanceApi;
    @Resource
    private BpmProcessInstanceService processInstanceService;
    @Resource
    private BpmApprovalEventDispatcher approvalEventDispatcher;

    private Map<String, ApprovalContextProvider> contextProviderMap;
    private Map<String, ApprovalResultHandler> resultHandlerMap;

    /**
     * 注入所有 ApprovalContextProvider，启动时校验 sceneCode 唯一性
     */
    @Resource
    public void setContextProviders(List<ApprovalContextProvider> providers) {
        this.contextProviderMap = CollectionUtils.convertMap(providers, ApprovalContextProvider::getSceneCode);
        validateUniqueSceneCodes(providers.stream()
                .map(ApprovalContextProvider::getSceneCode).collect(Collectors.toList()),
                "ApprovalContextProvider");
    }

    /**
     * 注入所有 ApprovalResultHandler，启动时校验 sceneCode 唯一性
     */
    @Resource
    public void setResultHandlers(List<ApprovalResultHandler> handlers) {
        this.resultHandlerMap = CollectionUtils.convertMap(handlers, ApprovalResultHandler::getSceneCode);
        validateUniqueSceneCodes(handlers.stream()
                .map(ApprovalResultHandler::getSceneCode).collect(Collectors.toList()),
                "ApprovalResultHandler");
    }

    private void validateUniqueSceneCodes(List<String> codes, String componentType) {
        Set<String> unique = codes.stream().collect(Collectors.toSet());
        if (unique.size() != codes.size()) {
            throw new IllegalStateException("[" + componentType + "] 存在重复的 sceneCode，请检查配置");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(String sceneCode, Long bizId, Long userId) {
        // 0. 参数校验
        if (StrUtil.isBlank(sceneCode)) {
            throw exception(APPROVAL_SCENE_NOT_EXISTS);
        }

        // 1. 校验场景存在（getSceneByCode 已内置不存在时抛异常）
        BpmApprovalSceneRespVO scene = approvalSceneService.getSceneByCode(sceneCode);
        if (!ObjectUtil.equal(scene.getStatus(), BpmApprovalSceneStatusEnum.ENABLED.getStatus())) {
            throw exception(APPROVAL_SCENE_DISABLED);
        }

        // 2. 获取上下文提供者
        ApprovalContextProvider contextProvider = contextProviderMap.get(sceneCode);
        if (contextProvider == null) {
            throw exception(APPROVAL_CONTEXT_PROVIDER_NOT_FOUND, sceneCode);
        }

        // 3. 获取业务上下文
        ApprovalContext context = contextProvider.getContext(bizId);
        if (context == null) {
            throw exception(APPROVAL_CONTEXT_IS_NULL, sceneCode, bizId);
        }

        // 4. 获取当前正式审批方案
        Long activeSchemeId = scene.getActiveSchemeId();
        if (activeSchemeId == null) {
            throw exception(APPROVAL_SCHEME_NOT_ACTIVE, sceneCode);
        }
        BpmApprovalSchemeVersionDO activeVersion = approvalSchemeVersionMapper.selectBySchemeIdAndStatus(
                activeSchemeId, BpmApprovalSchemeStatusEnum.ACTIVE.getStatus());
        if (activeVersion == null) {
            throw exception(APPROVAL_SCHEME_VERSION_NOT_ACTIVE, activeSchemeId);
        }

        // 5. 命中规则（暂时使用默认规则）
        List<BpmApprovalRuleDO> rules = approvalRuleMapper.selectListBySchemeVersionId(activeVersion.getId());
        BpmApprovalRuleDO hitRule = rules.stream()
                .filter(rule -> Boolean.TRUE.equals(rule.getDefaultRule()) && Boolean.TRUE.equals(rule.getEnabled()))
                .findFirst()
                .orElse(null);
        if (hitRule == null) {
            throw exception(APPROVAL_RULE_NOT_FOUND, activeVersion.getId());
        }

        // 6. 创建运行时快照（本地 DB，事务内完成）
        String processKey = hitRule.getProcessJson();
        BpmApprovalInstanceSnapshotDO snapshot = BpmApprovalInstanceSnapshotDO.builder()
                .sceneCode(sceneCode)
                .bizId(String.valueOf(bizId))
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

        // 7. 注册事务提交后回调：事务成功后再调 BPM 引擎，避免外部调用成功但本地回滚导致孤儿流程
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    Map<String, Object> variables = new HashMap<>(context.getVariables());
                    variables.put("sceneCode", sceneCode);
                    variables.put("bizId", String.valueOf(bizId));
                    variables.put("snapshotId", snapshotId);

                    String processInstanceId = processInstanceApi.createProcessInstance(userId,
                            new BpmProcessInstanceCreateReqDTO()
                                    .setProcessDefinitionKey(processKey)
                                    .setBusinessKey(String.valueOf(bizId))
                                    .setVariables(variables));
                    approvalInstanceSnapshotService.updateSnapshotProcessInstanceId(snapshotId, processInstanceId);
                } catch (Exception e) {
                    // BPM 创建失败，将快照标记为失败，避免留下 PROCESSING + 无 processInstanceId 的不可恢复脏数据
                    log.error("[submit][场景({}) 业务({}) BPM引擎创建流程实例失败，快照标记为失败]",
                            sceneCode, bizId, e);
                    approvalInstanceSnapshotService.updateSnapshotStatus(snapshotId,
                            BpmApprovalInstanceSnapshotStatusEnum.FAILED.getStatus(), "BPM创建失败: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public BpmApprovalDetailRespVO getApprovalDetail(String sceneCode, Long bizId) {
        BpmApprovalInstanceSnapshotDO snapshot = approvalInstanceSnapshotService
                .getSnapshotBySceneCodeAndBizId(sceneCode, String.valueOf(bizId));
        if (snapshot == null) {
            throw exception(APPROVAL_INSTANCE_SNAPSHOT_NOT_EXISTS);
        }
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
    @Transactional(rollbackFor = Exception.class)
    public void cancel(String sceneCode, Long bizId, Long userId, String reason) {
        // 1. 查找快照
        BpmApprovalInstanceSnapshotDO snapshot = approvalInstanceSnapshotService
                .getSnapshotBySceneCodeAndBizId(sceneCode, String.valueOf(bizId));
        if (snapshot == null) {
            throw exception(APPROVAL_INSTANCE_SNAPSHOT_NOT_EXISTS);
        }
        if (!ObjectUtil.equal(snapshot.getStatus(), BpmApprovalInstanceSnapshotStatusEnum.PROCESSING.getStatus())) {
            throw exception(APPROVAL_INSTANCE_NOT_PROCESSING);
        }

        // 2. 校验流程实例存在
        if (StrUtil.isBlank(snapshot.getProcessInstanceId())) {
            throw exception(APPROVAL_PROCESS_INSTANCE_NOT_EXISTS);
        }

        // 3. 同步调用 BPM 引擎撤回（在事务内，失败则整个事务回滚，snapshot 保持 PROCESSING 可重试）
        processInstanceService.cancelProcessInstanceByStartUser(userId,
                new BpmProcessInstanceCancelReqVO()
                        .setId(snapshot.getProcessInstanceId())
                        .setReason(reason));

        // 4. 快照状态更新和 handler.onCancel() 由 BPM CANCEL 事件统一驱动（通过 BpmProcessInstanceEventListener → BpmApprovalEventDispatcher）
        //    不在此处重复更新，避免与事件分发器双重写入
    }

    @Override
    public void dispatchResult(BpmProcessInstanceStatusEvent event) {
        approvalEventDispatcher.onApplicationEvent(event);
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

}
