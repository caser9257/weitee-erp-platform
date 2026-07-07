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
 * 财务模块 BPMN 流程定义自动部署工具。
 *
 * 使用方式：运行此测试即可将财务审批 BPMN 部署到 Flowable 引擎，
 * 同时写入 bpm_process_definition_info 扩展元数据。
 */
@Transactional
class FinanceBpmnDeployTest extends BaseDbUnitTest {

    @Resource
    private RepositoryService repositoryService;

    @Resource
    private BpmProcessDefinitionInfoMapper processDefinitionInfoMapper;

    private static final String[][] BPMN_FILES = {
            {"erp_finance_payment", "付款单审批", "bpmn/erp_finance_payment.bpmn"},
            {"erp_finance_expense", "费用报销审批", "bpmn/erp_finance_expense.bpmn"},
    };

    @Test
    void deployAllFinanceBpmn() throws Exception {
        for (String[] spec : BPMN_FILES) {
            deployOne(spec[0], spec[1], spec[2]);
        }
    }

    private void deployOne(String processKey, String processName, String classpath) throws Exception {
        ClassPathResource resource = new ClassPathResource(classpath);
        byte[] bpmnBytes = resource.getContentAsString(StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8);

        Deployment deployment = repositoryService.createDeployment()
                .key(processKey)
                .name(processName)
                .addBytes(processKey + ".bpmn", bpmnBytes)
                .tenantId(ProcessEngineConfiguration.NO_TENANT_ID)
                .disableSchemaValidation()
                .deploy();

        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId()).singleResult();
        assertNotNull(definition, "部署后 ProcessDefinition 不应为空: " + processKey);
        assertEquals(processKey, definition.getKey());

        BpmProcessDefinitionInfoDO info = BpmProcessDefinitionInfoDO.builder()
                .processDefinitionId(definition.getId())
                .modelId(null)
                .modelType(BpmModelTypeEnum.SIMPLE.getType())
                .category(null)
                .description(processName + " - 财务 BPM 审批流程（自动部署）")
                .formType(null)
                .visible(true)
                .sort(0L)
                .allowCancelRunningProcess(true)
                .allowWithdrawTask(true)
                .build();
        processDefinitionInfoMapper.insert(info);

        System.out.println("[FinanceBpmnDeploy] 部署成功: processKey=" + processKey
                + ", definitionId=" + definition.getId() + ", deploymentId=" + deployment.getId());
    }
}
