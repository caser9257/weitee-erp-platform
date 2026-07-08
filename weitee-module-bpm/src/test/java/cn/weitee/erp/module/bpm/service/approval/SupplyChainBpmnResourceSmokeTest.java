package cn.weitee.erp.module.bpm.service.approval;

import cn.weitee.erp.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SupplyChainBpmnResourceSmokeTest {

    @Test
    void shouldContainSupplyChainApprovalBpmnResources() {
        assertTrue(new ClassPathResource("bpmn/erp_purchase_return_approval.bpmn").exists(),
                "缺少采购退货审批 BPMN 资源: bpmn/erp_purchase_return_approval.bpmn");
        assertTrue(new ClassPathResource("bpmn/erp_stock_in_approval.bpmn").exists(),
                "缺少其它入库审批 BPMN 资源: bpmn/erp_stock_in_approval.bpmn");
        assertTrue(new ClassPathResource("bpmn/erp_stock_out_approval.bpmn").exists(),
                "缺少其它出库审批 BPMN 资源: bpmn/erp_stock_out_approval.bpmn");
    }

    @Test
    void stockApprovalBpmnShouldDeclareCandidateStrategy() throws Exception {
        assertStockApprovalCandidateConfig("bpmn/erp_stock_in_approval.bpmn");
        assertStockApprovalCandidateConfig("bpmn/erp_stock_out_approval.bpmn");
    }

    private static void assertStockApprovalCandidateConfig(String classpath) throws Exception {
        BpmnModel model = readBpmnModel(classpath);

        FlowElement level1Task = BpmnModelUtils.getFlowElementById(model, "task_approve_level1");
        assertNotNull(level1Task, classpath + " 缺少一级审批节点");
        assertEquals(37, BpmnModelUtils.parseCandidateStrategy(level1Task),
                classpath + " 一级审批缺少部门负责人候选策略");
        assertEquals("1", BpmnModelUtils.parseCandidateParam(level1Task),
                classpath + " 一级审批缺少部门负责人候选参数");
        assertEquals(3, BpmnModelUtils.parseAssignStartUserHandlerType(level1Task),
                classpath + " 一级审批缺少发起人处理策略");
        assertEquals(4, BpmnModelUtils.parseAssignEmptyHandlerType(level1Task),
                classpath + " 一级审批缺少空审批人处理策略");

        FlowElement level2Task = BpmnModelUtils.getFlowElementById(model, "task_approve_level2");
        assertNotNull(level2Task, classpath + " 缺少二级审批节点");
        assertEquals(38, BpmnModelUtils.parseCandidateStrategy(level2Task),
                classpath + " 二级审批缺少岗位候选策略");
        assertEquals("2", BpmnModelUtils.parseCandidateParam(level2Task),
                classpath + " 二级审批缺少总经理岗位候选参数");
        assertEquals(3, BpmnModelUtils.parseAssignStartUserHandlerType(level2Task),
                classpath + " 二级审批缺少发起人处理策略");
        assertEquals(4, BpmnModelUtils.parseAssignEmptyHandlerType(level2Task),
                classpath + " 二级审批缺少空审批人处理策略");
    }

    private static BpmnModel readBpmnModel(String classpath) throws Exception {
        ClassPathResource resource = new ClassPathResource(classpath);
        return BpmnModelUtils.getBpmnModel(resource.getContentAsByteArray());
    }
}
