package cn.weitee.erp.module.bpm.service.approval.engine;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * 审批规则条件求值引擎
 *
 * 支持的条件结构：
 * <pre>
 * {
 *   "conditions": [
 *     { "field": "amount", "operator": ">", "value": 10000 },
 *     { "field": "deptId", "operator": "==", "value": 5 }
 *   ],
 *   "logic": "AND"
 * }
 * </pre>
 *
 * 支持的操作符：==, !=, >, <, >=, <=, in, not_in, contains, between
 * 支持的逻辑组合：AND, OR
 *
 * @author system
 */
@Component
@Slf4j
public class RuleConditionEvaluator {

    private static final Set<String> NUMERIC_FIELDS = Set.of(
            "amount", "deptId", "projectId", "startUserId", "organId", "bizId");

    /**
     * 评估规则条件是否匹配
     *
     * @param conditionJson 条件 JSON 字符串
     * @param context 审批上下文
     * @return 是否匹配
     */
    public boolean evaluate(String conditionJson, ApprovalContext context) {
        // 空条件视为匹配（兼容无条件规则）
        if (StrUtil.isBlank(conditionJson)) {
            return true;
        }

        try {
            Map<String, Object> conditionMap = JSONUtil.toBean(conditionJson, Map.class);
            return evaluateCondition(conditionMap, context);
        } catch (Exception e) {
            log.warn("[evaluate] 条件解析失败，conditionJson={}", conditionJson, e);
            return false;
        }
    }

    /**
     * 递归评估条件
     */
    @SuppressWarnings("unchecked")
    private boolean evaluateCondition(Map<String, Object> condition, ApprovalContext context) {
        // 1. 叶子条件：包含 field, operator, value
        if (condition.containsKey("field") && condition.containsKey("operator")) {
            return evaluateLeaf(condition, context);
        }

        // 2. 组合条件：包含 conditions 和 logic
        if (condition.containsKey("conditions") && condition.containsKey("logic")) {
            List<Map<String, Object>> conditions = (List<Map<String, Object>>) condition.get("conditions");
            String logic = String.valueOf(condition.get("logic")).toUpperCase();

            if (CollUtil.isEmpty(conditions)) {
                return true;
            }

            if ("OR".equals(logic)) {
                return conditions.stream().anyMatch(c -> evaluateCondition(c, context));
            } else {
                // 默认 AND
                return conditions.stream().allMatch(c -> evaluateCondition(c, context));
            }
        }

        log.warn("[evaluateCondition] 未知的条件结构：{}", condition);
        return false;
    }

    /**
     * 评估叶子条件
     */
    private boolean evaluateLeaf(Map<String, Object> condition, ApprovalContext context) {
        String field = String.valueOf(condition.get("field"));
        String operator = String.valueOf(condition.get("operator"));
        Object expectedValue = condition.get("value");

        // 从上下文获取实际值
        Object actualValue = getFieldValue(field, context);
        if (actualValue == null && expectedValue == null) {
            return "==".equals(operator);
        }
        if (actualValue == null) {
            return "!=".equals(operator) || "not_in".equals(operator);
        }

        switch (operator) {
            case "==":
            case "equals":
                return compareValues(field, actualValue, expectedValue) == 0;

            case "!=":
            case "not_equals":
                return compareValues(field, actualValue, expectedValue) != 0;

            case ">":
            case "gt":
                return compareValues(field, actualValue, expectedValue) > 0;

            case "<":
            case "lt":
                return compareValues(field, actualValue, expectedValue) < 0;

            case ">=":
            case "gte":
                return compareValues(field, actualValue, expectedValue) >= 0;

            case "<=":
            case "lte":
                return compareValues(field, actualValue, expectedValue) <= 0;

            case "in":
                return evaluateIn(actualValue, expectedValue);

            case "not_in":
                return !evaluateIn(actualValue, expectedValue);

            case "contains":
                return evaluateContains(actualValue, expectedValue);

            case "between":
                return evaluateBetween(actualValue, expectedValue);

            default:
                log.warn("[evaluateLeaf] 未知的操作符：{}", operator);
                return false;
        }
    }

    /**
     * 从上下文获取字段值
     */
    private Object getFieldValue(String field, ApprovalContext context) {
        // 优先从 variables 中获取
        Map<String, Object> variables = context.getVariables();
        if (variables != null && variables.containsKey(field)) {
            return variables.get(field);
        }

        // 再从上下文直接字段获取
        switch (field) {
            case "amount":
                return context.getAmount();
            case "deptId":
                return context.getDeptId();
            case "projectId":
                return context.getProjectId();
            case "startUserId":
                return context.getStartUserId();
            case "organId":
                return context.getOrganId();
            case "bizId":
                return context.getBizId();
            case "bizNo":
                return context.getBizNo();
            default:
                return null;
        }
    }

    /**
     * 比较两个值
     *
     * @return 负数：actual < expected；0：相等；正数：actual > expected
     */
    private int compareValues(String field, Object actual, Object expected) {
        // 数值字段可能来自 JSON 字符串，避免回退到字典序比较
        if ((actual instanceof Number || expected instanceof Number || isNumericField(field))
                && isNumericValue(actual) && isNumericValue(expected)) {
            try {
                BigDecimal actualDecimal = new BigDecimal(String.valueOf(actual));
                BigDecimal expectedDecimal = new BigDecimal(String.valueOf(expected));
                return actualDecimal.compareTo(expectedDecimal);
            } catch (NumberFormatException ignored) {
                // 非法数字按字符串比较，最终由规则结果决定是否命中
            }
        }

        // 字符串比较
        String actualStr = String.valueOf(actual);
        String expectedStr = String.valueOf(expected);
        return actualStr.compareTo(expectedStr);
    }

    private boolean isNumericField(String field) {
        return NUMERIC_FIELDS.contains(field);
    }

    private boolean isNumericValue(Object value) {
        if (value instanceof Number) {
            return true;
        }
        if (!(value instanceof CharSequence) || StrUtil.isBlank(value.toString())) {
            return false;
        }
        try {
            new BigDecimal(value.toString().trim());
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    /**
     * 评估 in 操作
     */
    @SuppressWarnings("unchecked")
    private boolean evaluateIn(Object actualValue, Object expectedValue) {
        Collection<?> expectedCollection;
        if (expectedValue instanceof Collection) {
            expectedCollection = (Collection<?>) expectedValue;
        } else if (expectedValue instanceof String) {
            // 支持逗号分隔的字符串："1,2,3"
            expectedCollection = Arrays.asList(((String) expectedValue).split(","));
        } else {
            expectedCollection = Collections.singletonList(expectedValue);
        }

        String actualStr = String.valueOf(actualValue);
        return expectedCollection.stream()
                .anyMatch(item -> actualStr.equals(String.valueOf(item).trim()));
    }

    /**
     * 评估 contains 操作
     */
    private boolean evaluateContains(Object actualValue, Object expectedValue) {
        String actualStr = String.valueOf(actualValue);
        String expectedStr = String.valueOf(expectedValue);
        return actualStr.contains(expectedStr);
    }

    /**
     * 评估 between 操作
     *
     * expectedValue 应为数组或逗号分隔字符串："min,max"
     */
    @SuppressWarnings("unchecked")
    private boolean evaluateBetween(Object actualValue, Object expectedValue) {
        List<?> range;
        if (expectedValue instanceof List) {
            range = (List<?>) expectedValue;
        } else if (expectedValue instanceof String) {
            range = Arrays.asList(((String) expectedValue).split(","));
        } else {
            return false;
        }

        if (range.size() < 2) {
            return false;
        }

        BigDecimal actual = new BigDecimal(String.valueOf(actualValue));
        BigDecimal min = new BigDecimal(String.valueOf(range.get(0)).trim());
        BigDecimal max = new BigDecimal(String.valueOf(range.get(1)).trim());

        return actual.compareTo(min) >= 0 && actual.compareTo(max) <= 0;
    }

}
