package cn.weitee.erp.module.bpm.service.approval.engine;

import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleConditionEvaluatorTest {

    private final RuleConditionEvaluator evaluator = new RuleConditionEvaluator();

    @Mock
    private ApprovalContext context;

    @Test
    void evaluate_shouldCompareNumericJsonStringAsNumber() {
        when(context.getVariables()).thenReturn(Collections.emptyMap());
        when(context.getAmount()).thenReturn(new BigDecimal("900"));

        assertFalse(evaluator.evaluate("{\"conditions\":[{\"field\":\"amount\",\"operator\":\">\",\"value\":\"10000\"}],\"logic\":\"AND\"}", context));
        assertTrue(evaluator.evaluate("{\"conditions\":[{\"field\":\"amount\",\"operator\":\">\",\"value\":\"800\"}],\"logic\":\"AND\"}", context));
    }

    @Test
    void evaluate_shouldCompareNumericVariableStringsAsNumbers() {
        when(context.getVariables()).thenReturn(Collections.singletonMap("amount", "900"));

        assertFalse(evaluator.evaluate("{\"conditions\":[{\"field\":\"amount\",\"operator\":\">\",\"value\":\"10000\"}],\"logic\":\"AND\"}", context));
        assertTrue(evaluator.evaluate("{\"conditions\":[{\"field\":\"amount\",\"operator\":\">\",\"value\":\"800\"}],\"logic\":\"AND\"}", context));
    }

    @Test
    void evaluate_shouldKeepStringIdentifierComparison() {
        when(context.getVariables()).thenReturn(Collections.emptyMap());
        when(context.getBizNo()).thenReturn("00123");

        assertFalse(evaluator.evaluate("{\"conditions\":[{\"field\":\"bizNo\",\"operator\":\"==\",\"value\":\"123\"}],\"logic\":\"AND\"}", context));
        assertTrue(evaluator.evaluate("{\"conditions\":[{\"field\":\"bizNo\",\"operator\":\"==\",\"value\":\"00123\"}],\"logic\":\"AND\"}", context));
    }

    @Test
    void evaluate_shouldSupportNestedGroups() {
        when(context.getVariables()).thenReturn(Collections.emptyMap());
        when(context.getAmount()).thenReturn(new BigDecimal("12000"));
        when(context.getDeptId()).thenReturn(3L);

        String conditionJson = "{\"conditions\":[{\"conditions\":[{\"field\":\"amount\",\"operator\":\">\",\"value\":10000},{\"field\":\"deptId\",\"operator\":\"==\",\"value\":3}],\"logic\":\"AND\"}],\"logic\":\"OR\"}";

        assertTrue(evaluator.evaluate(conditionJson, context));
    }

    @Test
    void evaluate_shouldReturnFalseForInvalidJson() {
        assertFalse(evaluator.evaluate("not-json", context));
    }
}
