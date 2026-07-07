package cn.weitee.erp.module.bpm.service.approval;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceBpmnResourceSmokeTest {

    @Test
    void shouldContainFinanceApprovalBpmnResources() {
        assertTrue(new ClassPathResource("bpmn/erp_finance_payment.bpmn").exists(),
                "缺少付款单审批 BPMN 资源: bpmn/erp_finance_payment.bpmn");
        assertTrue(new ClassPathResource("bpmn/erp_finance_expense.bpmn").exists(),
                "缺少费用报销审批 BPMN 资源: bpmn/erp_finance_expense.bpmn");
    }
}
