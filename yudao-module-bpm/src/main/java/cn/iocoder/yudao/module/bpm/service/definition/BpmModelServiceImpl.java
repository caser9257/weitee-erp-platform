package cn.iocoder.yudao.module.bpm.service.definition;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelMetaInfoVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelSaveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelUpdateReqVO;
import cn.iocoder.yudao.module.bpm.convert.definition.BpmModelConvert;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmFormDO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelFormTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmReasonEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateInvoker;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.SimpleModelUtils;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceCopyService;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.enums.permission.RoleCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.impl.db.SuspensionState;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ModelQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils.parseCandidateStrategy;

/**
 * BPM model service implementation for Flowable {@link Model} management.
 *
 * @author yunlongn
 * @author 鑺嬮亾婧愮爜
 * @author jason
 */
@Service
@Validated
@Slf4j
public class BpmModelServiceImpl implements BpmModelService {

    @Resource
    private RepositoryService repositoryService;
    @Resource
    private BpmProcessDefinitionService processDefinitionService;
    @Resource
    private BpmFormService bpmFormService;

    @Resource
    private BpmTaskCandidateInvoker taskCandidateInvoker;

    @Resource
    private HistoryService historyService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private TaskService taskService;
    @Resource
    private BpmProcessInstanceCopyService processInstanceCopyService;
    @Resource
    private PermissionApi permissionApi;

    @Override
    public List<Model> getModelList(String name) {
        ModelQuery modelQuery = repositoryService.createModelQuery();
        if (StrUtil.isNotEmpty(name)) {
            modelQuery.modelNameLike("%" + name + "%");
        }
        modelQuery.modelTenantId(ProcessEngineConfiguration.NO_TENANT_ID);
        return modelQuery.list();
    }

    @Override
    public Long getModelCountByCategory(String category) {
        return repositoryService.createModelQuery()
                .modelCategory(category)
                .modelTenantId(ProcessEngineConfiguration.NO_TENANT_ID)
                .count();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createModel(@Valid BpmModelSaveReqVO createReqVO) {
        if (!ValidationUtils.isXmlNCName(createReqVO.getKey())) {
            throw exception(MODEL_KEY_VALID);
        }
        // 1. 鏍￠獙娴佺▼鏍囪瘑宸茬粡瀛樺湪
        Model keyModel = getModelByKey(createReqVO.getKey());
        if (keyModel != null) {
            throw exception(MODEL_KEY_EXISTS, createReqVO.getKey());
        }

        // 2. 鍒涘缓 Model 瀵硅薄
        createReqVO.setSort(System.currentTimeMillis()); // Use current time as the default sort value.
        Model model = repositoryService.newModel();
        BpmModelConvert.INSTANCE.copyToModel(model, createReqVO);
        model.setTenantId(ProcessEngineConfiguration.NO_TENANT_ID);

        // 3. 淇濆瓨妯″瀷
        saveModel(model, createReqVO);
        return model.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // Keep multi-step updates in one transaction.
    public void updateModel(Long userId, BpmModelSaveReqVO updateReqVO) {
        // 1. 鏍￠獙娴佺▼妯″瀷瀛樺湪
        Model model = validateModelManager(updateReqVO.getId(), userId);

        // 2. 濉厖 Model 淇℃伅
        BpmModelConvert.INSTANCE.copyToModel(model, updateReqVO);

        // 3. 淇濆瓨妯″瀷
        saveModel(model, updateReqVO);
    }

    /**
     * Save model metadata and process content.
     *
     * @param model model entity
     * @param saveReqVO save request
     */
    private void saveModel(Model model, BpmModelSaveReqVO saveReqVO) {
        updateModelPublishState(model, true);
        // 1. 淇濆瓨妯″瀷鐨勫熀纭€淇℃伅
        repositoryService.saveModel(model);

        // 2. Save process content.
        if (ObjUtil.equals(BpmModelTypeEnum.BPMN.getType(), saveReqVO.getType())
                && StrUtil.isNotEmpty(saveReqVO.getBpmnXml())) {
            doUpdateModelBpmnXml(model.getId(), saveReqVO.getBpmnXml());
        } else if (ObjUtil.equals(BpmModelTypeEnum.SIMPLE.getType(), saveReqVO.getType())
                && saveReqVO.getSimpleModel() != null) {
            // Convert JSON to a BPMN model.
            BpmnModel bpmnModel = SimpleModelUtils.buildBpmnModel(model.getKey(), model.getName(),
                    saveReqVO.getSimpleModel());
            // 淇濆瓨 Bpmn XML
            doUpdateModelBpmnXml(model.getId(), BpmnModelUtils.getBpmnXml(bpmnModel));
            // 淇濆瓨 JSON 鏁版嵁
            updateModelSimpleJson(model.getId(), saveReqVO.getSimpleModel());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateModelSortBatch(Long userId, List<String> ids) {
        // 1.1 鏍￠獙娴佺▼妯″瀷瀛樺湪
        List<Model> models = repositoryService.createModelQuery()
                .modelTenantId(ProcessEngineConfiguration.NO_TENANT_ID).list();
        models.removeIf(model -> !ids.contains(model.getId()));
        if (ids.size() != models.size()) {
            throw exception(MODEL_NOT_EXISTS);
        }
        Map<String, Model> modelMap = convertMap(models, Model::getId);
        // 1.2 鏍￠獙鏄惁涓虹鐞嗗憳
        ids.forEach(id -> validateModelManager(id, userId));

        // 淇濆瓨鎺掑簭
        long sort = System.currentTimeMillis(); // Use descending timestamps to preserve order.
        for (int i = ids.size() - 1; i > 0; i--) {
            Model model = modelMap.get(ids.get(i));
            // 鏇存柊妯″瀷
            BpmModelMetaInfoVO metaInfo = BpmModelConvert.INSTANCE.parseMetaInfo(model).setSort(sort);
            model.setMetaInfo(JsonUtils.toJsonString(metaInfo));
            repositoryService.saveModel(model);
            // 鏇存柊鎺掑簭
            processDefinitionService.updateProcessDefinitionSortByModelId(model.getId(), sort);
            sort--;
        }
    }

    private Model validateModelExists(String id) {
        Model model = repositoryService.getModel(id);
        if (model == null) {
            throw exception(MODEL_NOT_EXISTS);
        }
        return model;
    }

    /**
     * Validate whether the user can manage the model.
     *
     * @param id model id
     * @param userId user id
     * @return model
     */
    private Model validateModelManager(String id, Long userId) {
        Model model = validateModelExists(id);
        BpmModelMetaInfoVO metaInfo = BpmModelConvert.INSTANCE.parseMetaInfo(model);
        if (metaInfo == null || (!CollUtil.contains(metaInfo.getManagerUserIds(), userId)
                && !permissionApi.hasAnyRoles(userId, RoleCodeEnum.SUPER_ADMIN.getCode()))) {
            throw exception(MODEL_UPDATE_FAIL_NOT_MANAGER, model.getName());
        }
        return model;
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // Keep deploy steps in one transaction.
    public void deployModel(Long userId, String id) {
        // 1.1 鏍￠獙娴佺▼妯″瀷瀛樺湪
        Model model = validateModelManager(id, userId);
        BpmModelMetaInfoVO metaInfo = BpmModelConvert.INSTANCE.parseMetaInfo(model);
        // 1.2 Validate process content.
        byte[] bpmnBytes = getModelBpmnXML(model.getId());
        validateBpmnXml(bpmnBytes, metaInfo.getType());
        // 1.3 鏍￠獙琛ㄥ崟宸查厤
        BpmFormDO form = validateFormConfig(metaInfo);
        // 1.4 Validate task candidate rules.
        taskCandidateInvoker.validateBpmnConfig(bpmnBytes);
        // 1.5 鑾峰彇浠块拤閽夋祦绋嬭璁″櫒妯″瀷鏁版嵁
        String simpleJson = getModelSimpleJson(model.getId());

        // 2.1 鍒涘缓娴佺▼瀹氫箟
        String definitionId = processDefinitionService.createProcessDefinition(model, metaInfo, bpmnBytes, simpleJson,
                form);

        // 2.2 Suspend the previous deployed definition so only the latest version can start new tasks.
        updateProcessDefinitionSuspended(model.getDeploymentId());

        // 2.3 Update the model deployment binding.
        ProcessDefinition definition = processDefinitionService.getProcessDefinition(definitionId);
        model.setDeploymentId(definition.getDeploymentId());
        updateModelPublishState(model, false);
        repositoryService.saveModel(model);
    }

    private void validateBpmnXml(byte[] bpmnBytes, Integer type) {
        BpmnModel bpmnModel = BpmnModelUtils.getBpmnModel(bpmnBytes);
        if (bpmnModel == null) {
            throw exception(MODEL_NOT_EXISTS);
        }
        // 1. 娌℃湁 StartEvent
        StartEvent startEvent = BpmnModelUtils.getStartEvent(bpmnModel);
        if (startEvent == null) {
            throw exception(MODEL_DEPLOY_FAIL_BPMN_START_EVENT_NOT_EXISTS);
        }
        // 2. Ensure every UserTask has a name.
        List<UserTask> userTasks = BpmnModelUtils.getBpmnModelElements(bpmnModel, UserTask.class);
        userTasks.forEach(userTask -> {
            if (StrUtil.isEmpty(userTask.getName())) {
                throw exception(MODEL_DEPLOY_FAIL_BPMN_USER_TASK_NAME_NOT_EXISTS, userTask.getId());
            }
        });
        // 3. 鏍￠獙绗竴涓敤鎴蜂换鍔¤妭鐐圭殑瑙勫垯绫诲瀷鏄惁涓衡€滃鎵逛汉鑷€夆€濓紝BPMN 璁捐鍣紝鏍￠獙绗竴涓敤鎴蜂换鍔¤妭鐐癸紝SIMPLE 璁捐鍣紝绗竴涓妭鐐瑰浐瀹氫负鍙戣捣浜烘墍浠ユ牎楠岀浜屼釜鐢ㄦ埛浠诲姟鑺傜偣
        UserTask firUserTask = CollUtil.get(userTasks, BpmModelTypeEnum.BPMN.getType().equals(type) ? 0 : 1);
        if (firUserTask == null) {
            return;
        }
        Integer candidateStrategy = parseCandidateStrategy(firUserTask);
        if (Objects.equals(candidateStrategy, BpmTaskCandidateStrategyEnum.APPROVE_USER_SELECT.getStrategy())) {
            throw exception(MODEL_DEPLOY_FAIL_FIRST_USER_TASK_CANDIDATE_STRATEGY_ERROR, firUserTask.getName());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteModel(Long userId, String id) {
        // 鏍￠獙娴佺▼妯″瀷瀛樺湪
        Model model = validateModelManager(id, userId);

        // 鎵ц鍒犻櫎
        repositoryService.deleteModel(id);
        // 绂佺敤娴佺▼瀹氫箟
        updateProcessDefinitionSuspended(model.getDeploymentId());
    }

    @Override
    public void cleanModel(Long userId, String id) {
        // 1. 鏍￠獙娴佺▼妯″瀷瀛樺湪
        Model model = validateModelManager(id, userId);

        // 2. Clean all related process data.
        // 2.1 鍏堝彇娑堟墍鏈夋鍦ㄨ繍琛岀殑娴佺▼
        List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey(model.getKey()).list();
        processInstances.forEach(processInstance -> {
            runtimeService.deleteProcessInstance(processInstance.getId(),
                    BpmReasonEnum.CANCEL_BY_SYSTEM.getReason());
            historyService.deleteHistoricProcessInstance(processInstance.getId());
            processInstanceCopyService.deleteProcessInstanceCopy(processInstance.getId());
        });
        // 2.2 鍐嶄粠鍘嗗彶涓垹闄ゆ墍鏈夌浉鍏崇殑娴佺▼鏁版嵁
        List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(model.getKey()).list();
        historicProcessInstances.forEach(historicProcessInstance -> {
            historyService.deleteHistoricProcessInstance(historicProcessInstance.getId());
            processInstanceCopyService.deleteProcessInstanceCopy(historicProcessInstance.getId());
        });
        // 2.3 Clean remaining runtime tasks.
        List<Task> tasks = taskService.createTaskQuery()
                .processDefinitionKey(model.getKey()).list();
        tasks.forEach(task -> taskService.deleteTask(task.getId(),BpmReasonEnum.CANCEL_BY_PROCESS_CLEAN.getReason()));
    }

    @Override
    public void updateModelState(Long userId, String id, Integer state) {
        // 1.1 鏍￠獙娴佺▼妯″瀷瀛樺湪
        Model model = validateModelManager(id, userId);
        // 1.2 鏍￠獙娴佺▼瀹氫箟瀛樺湪
        ProcessDefinition definition = processDefinitionService
                .getProcessDefinitionByDeploymentId(model.getDeploymentId());
        if (definition == null) {
            throw exception(PROCESS_DEFINITION_NOT_EXISTS);
        }

        // 2. Update process definition state.
        processDefinitionService.updateProcessDefinitionState(definition.getId(), state);
    }

    @Override
    public BpmnModel getBpmnModelByDefinitionId(String processDefinitionId) {
        return repositoryService.getBpmnModel(processDefinitionId);
    }

    @Override
    public BpmSimpleModelNodeVO getSimpleModel(String modelId) {
        Model model = validateModelExists(modelId);
        // Read the simple-model JSON from ACT_RE_MODEL.EDITOR_SOURCE_EXTRA_VALUE_ID_.
        String json = getModelSimpleJson(model.getId());
        return JsonUtils.parseObject(json, BpmSimpleModelNodeVO.class);
    }

    @Override
    public void updateSimpleModel(Long userId, BpmSimpleModelUpdateReqVO reqVO) {
        // 1. 鏍￠獙娴佺▼妯″瀷瀛樺湪
        Model model = validateModelManager(reqVO.getId(), userId);

        // 2.1 Convert JSON to a BPMN model.
        BpmnModel bpmnModel = SimpleModelUtils.buildBpmnModel(model.getKey(), model.getName(), reqVO.getSimpleModel());
        // 2.2 淇濆瓨 Bpmn XML
        doUpdateModelBpmnXml(model.getId(), BpmnModelUtils.getBpmnXml(bpmnModel));
        // 2.3 淇濆瓨 JSON 鏁版嵁
        updateModelSimpleJson(model.getId(), reqVO.getSimpleModel());
        updateModelPublishState(model, true);
        repositoryService.saveModel(model);
    }

    /**
     * Validate form configuration for model deployment.
     *
     * @param metaInfo model meta info
     * @return form config
     */
    private BpmFormDO validateFormConfig(BpmModelMetaInfoVO metaInfo) {
        if (metaInfo == null || metaInfo.getFormType() == null) {
            throw exception(MODEL_DEPLOY_FAIL_FORM_NOT_CONFIG);
        }
        // 鏍￠獙琛ㄥ崟瀛樺湪
        if (Objects.equals(metaInfo.getFormType(), BpmModelFormTypeEnum.NORMAL.getType())) {
            if (metaInfo.getFormId() == null) {
                throw exception(MODEL_DEPLOY_FAIL_FORM_NOT_CONFIG);
            }
            BpmFormDO form = bpmFormService.getForm(metaInfo.getFormId());
            if (form == null) {
                throw exception(FORM_NOT_EXISTS);
            }
            return form;
        } else {
            if (StrUtil.isEmpty(metaInfo.getFormCustomCreatePath())
                    || StrUtil.isEmpty(metaInfo.getFormCustomViewPath())) {
                throw exception(MODEL_DEPLOY_FAIL_FORM_NOT_CONFIG);
            }
            return null;
        }
    }

    @Override
    public void updateModelBpmnXml(String id, String bpmnXml) {
        if (StrUtil.isEmpty(bpmnXml)) {
            return;
        }
        doUpdateModelBpmnXml(id, bpmnXml);
        Model model = validateModelExists(id);
        updateModelPublishState(model, true);
        repositoryService.saveModel(model);
    }

    private void doUpdateModelBpmnXml(String id, String bpmnXml) {
        repositoryService.addModelEditorSource(id, StrUtil.utf8Bytes(bpmnXml));
    }

    @SuppressWarnings("JavaExistingMethodCanBeUsed")
    private String getModelSimpleJson(String id) {
        byte[] bytes = repositoryService.getModelEditorSourceExtra(id);
        if (ArrayUtil.isEmpty(bytes)) {
            return null;
        }
        return StrUtil.utf8Str(bytes);
    }

    private void updateModelSimpleJson(String id, BpmSimpleModelNodeVO node) {
        if (node == null) {
            return;
        }
        byte[] bytes = JsonUtils.toJsonByte(node);
        repositoryService.addModelEditorSourceExtra(id, bytes);
    }

    /**
     * Suspend the process definition bound to the deployment id.
     * <p>
     * A deployment id is expected to map to a single process definition here.
     *
     * @param deploymentId deployment id
     */
    private void updateProcessDefinitionSuspended(String deploymentId) {
        if (StrUtil.isEmpty(deploymentId)) {
            return;
        }
        ProcessDefinition oldDefinition = processDefinitionService.getProcessDefinitionByDeploymentId(deploymentId);
        if (oldDefinition == null) {
            return;
        }
        processDefinitionService.updateProcessDefinitionState(oldDefinition.getId(),
                SuspensionState.SUSPENDED.getStateCode());
    }

    private Model getModelByKey(String key) {
        return repositoryService.createModelQuery()
                .modelTenantId(ProcessEngineConfiguration.NO_TENANT_ID)
                .modelKey(key).singleResult();
    }

    @Override
    public Model getModel(String id) {
        return repositoryService.getModel(id);
    }

    @Override
    public byte[] getModelBpmnXML(String id) {
        return repositoryService.getModelEditorSource(id);
    }

    private void updateModelPublishState(Model model, boolean hasUnpublishedChanges) {
        BpmModelMetaInfoVO metaInfo = BpmModelConvert.INSTANCE.parseMetaInfo(model);
        if (metaInfo == null) {
            return;
        }
        metaInfo.setHasUnpublishedChanges(hasUnpublishedChanges);
        model.setMetaInfo(JsonUtils.toJsonString(metaInfo));
    }

}

