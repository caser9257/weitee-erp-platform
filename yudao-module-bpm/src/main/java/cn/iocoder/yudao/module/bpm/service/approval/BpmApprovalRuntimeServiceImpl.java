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

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Transactional(rollbackFor = Exception.class)
    public String submit(String sceneCode, Long bizId, Long userId) {
        // 1. 校验场景存在（getSceneByCode 已内置不存在时抛异常）
        BpmApprovalSceneRespVO scene = approvalSceneService.getSceneByCode(sceneCode);
        if (!ObjectUtil.equal(scene.getStatus(), BpmApprovalSceneStatusEnum.ENABLED.getStatus())) {
            throw exception(APPROVAL_SCENE_DISABLED);
        }

        // 2. 防重检查：同一业务单据在同一场景下不允许重复提交
        BpmApprovalInstanceSnapshotDO existingSnapshot = approvalInstanceSnapshotService
                .getSnapshotBySceneCodeAndBizId(sceneCode, String.valueOf(bizId));
        if (existingSnapshot != null) {
            if (ObjectUtil.equal(existingSnapshot.getStatus(),
                    BpmApprovalInstanceSnapshotStatusEnum.PROCESSING.getStatus())) {
                // 审批中，拒绝重复提交
                throw exception(APPROVAL_INSTANCE_ALREADY_PROCESSING, sceneCode, bizId);
            }
            // 已完结（通过/驳回/撤回），清理旧快照后允许重新提交
            approvalInstanceSnapshotService.deleteSnapshot(existingSnapshot.getId());
        }

        // 3. 获取上下文提供者
        ApprovalContextProvider contextProvider = contextProviderMap.get(sceneCode);
        if (contextProvider == null) {
            throw exception(APPROVAL_CONTEXT_PROVIDER_NOT_FOUND, sceneCode);
        }

        // 4. 获取业务上下文
        ApprovalContext context = contextProvider.getContext(bizId);
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

        // 6. 命中规则（暂时使用默认规则）
        List<BpmApprovalRuleDO> rules = approvalRuleMapper.selectListBySchemeVersionId(activeVersion.getId());
        BpmApprovalRuleDO hitRule = rules.stream()
                .filter(rule -> Boolean.TRUE.equals(rule.getDefaultRule()) && Boolean.TRUE.equals(rule.getEnabled()))
                .findFirst()
                .orElse(null);
        if (hitRule == null) {
            throw exception(APPROVAL_RULE_NOT_FOUND, activeVersion.getId());
        }

        // 7. 创建运行时快照
        // processDefinitionKey：BPM 启动所需流程定义 Key，取自规则的 processJson 字段
        // processJson：快照留存的流程配置 JSON，当前仅存 Key；未来扩展节点配置时在此追加
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

        // 8. 生成 BPM 启动参数
        Map<String, Object> variables = new HashMap<>(context.getVariables());
        variables.put("sceneCode", sceneCode);
        variables.put("bizId", String.valueOf(bizId));
        variables.put("snapshotId", snapshotId);

        // 9. 启动 BPM 实例
        String processInstanceId = processInstanceApi.createProcessInstance(userId,
                new BpmProcessInstanceCreateReqDTO()
                        .setProcessDefinitionKey(processKey)
                        .setBusinessKey(String.valueOf(bizId))
                        .setVariables(variables));

        // 10. 更新快照流程实例 ID
        approvalInstanceSnapshotService.updateSnapshotProcessInstanceId(snapshotId, processInstanceId);

        return processInstanceId;
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

        // 3. 调用 BPM 撤回
        processInstanceService.cancelProcessInstanceByStartUser(userId,
                new BpmProcessInstanceCancelReqVO()
                        .setId(snapshot.getProcessInstanceId())
                        .setReason(reason));

        // 4. 更新快照状态
        approvalInstanceSnapshotService.updateSnapshotStatus(snapshot.getId(),
                BpmApprovalInstanceSnapshotStatusEnum.CANCEL.getStatus(), reason);

        // 5. 调用结果处理器
        // 异常不做静默吞掉：若 onCancel() 抛出异常，快照状态保持 PROCESSING，避免标记撤回但业务未回写；
        // 调用方需感知失败并做补偿。若未来确认 onCancel() 仅为通知类副作用，可在实现内自行 try-catch。
        ApprovalResultHandler handler = resultHandlerMap.get(sceneCode);
        if (handler != null) {
            handler.onCancel(bizId, snapshot.getProcessInstanceId(), reason);
        }
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
