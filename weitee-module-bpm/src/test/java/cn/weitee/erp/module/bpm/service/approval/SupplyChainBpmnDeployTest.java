package cn.weitee.erp.module.bpm.service.approval;

import cn.weitee.erp.framework.test.core.ut.BaseDbUnitTest;
import cn.weitee.erp.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.weitee.erp.module.bpm.dal.mysql.definition.BpmProcessDefinitionInfoMapper;
import cn.weitee.erp.module.bpm.enums.definition.BpmModelTypeEnum;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 采购退货 / 其它入库 / 其它出库 — 3 个 BPMN 流程定义自动部署工具
 *
 * 使用方式：运行此测试即可自动部署 3 个 BPMN 流程定义到 Flowable 引擎，
 * 同时在 bpm_process_definition_info 表中插入扩展元数据。
 *
 * 部署完成后，BPM 审批流程的 processDefinitionKey 需要与 bpm_approval_rule.process_json 匹配：
 *   - erp_purchase_return_approval
 *   - erp_stock_in_approval
 *   - erp_stock_out_approval
 */
@Transactional
class SupplyChainBpmnDeployTest extends BaseDbUnitTest {

    @Resource
    private RepositoryService repositoryService;

    @Resource
    private BpmProcessDefinitionInfoMapper processDefinitionInfoMapper;

    private static final String[][] BPMN_FILES = {
            {"erp_purchase_return_approval", "采购退货审批", "bpmn/erp_purchase_return_approval.bpmn"},
            {"erp_stock_in_approval", "其它入库审批", "bpmn/erp_stock_in_approval.bpmn"},
            {"erp_stock_out_approval", "其它出库审批", "bpmn/erp_stock_out_approval.bpmn"},
    };

    @Test
    void deployAllSupplyChainBpmn() throws Exception {
        for (String[] spec : BPMN_FILES) {
            deployOne(spec[0], spec[1], spec[2]);
        }
    }

    private void deployOne(String processKey, String processName, String classpath) throws Exception {
        // 1. 读取 BPMN XML
        ClassPathResource resource = new ClassPathResource(classpath);
        byte[] bpmnBytes = resource.getContentAsString(StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8);

        // 2. 通过 Flowable RepositoryService 部署
        Deployment deployment = repositoryService.createDeployment()
                .key(processKey)
                .name(processName)
                .addBytes(processKey + ".bpmn", bpmnBytes)
                .tenantId(ProcessEngineConfiguration.NO_TENANT_ID)
                .disableSchemaValidation()
                .deploy();

        // 3. 获取部署后的 ProcessDefinition
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId()).singleResult();
        assertNotNull(definition, "部署后 ProcessDefinition 不应为空: " + processKey);
        assertEquals(processKey, definition.getKey());

        // 4. 插入扩展元数据（bpm_process_definition_info）
        BpmProcessDefinitionInfoDO info = BpmProcessDefinitionInfoDO.builder()
                .processDefinitionId(definition.getId())
                .modelId(null) // 无 Model 关联
                .modelType(BpmModelTypeEnum.SIMPLE.getType()) // 简单审批
                .category(null)
                .description(processName + " — 供应链 BPM 审批流程（自动部署）")
                .formType(null)
                .visible(true)
                .sort(0L)
                .allowCancelRunningProcess(true)
                .allowWithdrawTask(true)
                .build();
        processDefinitionInfoMapper.insert(info);

        System.out.println("[SupplyChainBpmnDeploy] 部署成功: processKey=" + processKey
                + ", definitionId=" + definition.getId() + ", deploymentId=" + deployment.getId());
    }
}
