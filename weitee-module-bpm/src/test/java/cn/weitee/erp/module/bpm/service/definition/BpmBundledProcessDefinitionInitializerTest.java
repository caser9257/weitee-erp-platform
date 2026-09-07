package cn.weitee.erp.module.bpm.service.definition;

import org.junit.jupiter.api.Test;
import cn.weitee.erp.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BpmBundledProcessDefinitionInitializerTest {

    @Test
    void shouldCreateProcessDefinitionInfoTableBeforeWritingBundledMetadata() {
        CapturingJdbcTemplate jdbcTemplate = new CapturingJdbcTemplate();
        BpmBundledProcessDefinitionInitializer initializer =
                new BpmBundledProcessDefinitionInitializer(null, null, jdbcTemplate);

        initializer.ensureProcessDefinitionInfoSchema();

        String sql = jdbcTemplate.executedSql.get(0);
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS `bpm_process_definition_info`"));
        assertTrue(sql.contains("`process_definition_id`"));
        assertTrue(sql.contains("`notification_policy_setting`"));
        assertTrue(sql.contains("`print_template_setting`"));
        assertTrue(jdbcTemplate.columnChecks.contains(List.of("bpm_process_definition_info", "notification_policy_setting")));
        assertTrue(jdbcTemplate.columnChecks.contains(List.of("bpm_process_definition_info", "print_template_setting")));
        assertTrue(jdbcTemplate.executedSql.stream()
                .anyMatch(item -> item.contains("ALTER TABLE `bpm_process_definition_info` ADD COLUMN `notification_policy_setting`")));
        assertTrue(jdbcTemplate.executedSql.stream()
                .anyMatch(item -> item.contains("ALTER TABLE `bpm_process_definition_info` ADD COLUMN `print_template_setting`")));
    }

    @Test
    void shouldBackfillBundledDefinitionInfoWithIdempotentSql() throws Exception {
        CapturingJdbcTemplate jdbcTemplate = new CapturingJdbcTemplate();
        BpmBundledProcessDefinitionInitializer initializer =
                new BpmBundledProcessDefinitionInitializer(null, null, jdbcTemplate);

        Method method = BpmBundledProcessDefinitionInitializer.class.getDeclaredMethod(
                "ensureDefinitionInfo",
                Class.forName("cn.weitee.erp.module.bpm.service.definition.BpmBundledProcessDefinitionInitializer$BundledProcessDefinition"),
                ProcessDefinition.class);
        method.setAccessible(true);
        Object bundledDefinition = createBundledDefinition("erp_stock_in_approval", "其它入库审批",
                "erp_stock", "/scm/stock-in", "/scm/stock-in");
        ProcessDefinition processDefinition = createProcessDefinition("auto-erp_stock_in_approval:49806b37:1");

        method.invoke(initializer, bundledDefinition, processDefinition);

        assertTrue(jdbcTemplate.updatedSql.stream()
                .anyMatch(item -> item.contains("UPDATE `bpm_process_definition_info`")));
        assertTrue(jdbcTemplate.updatedSql.stream()
                .noneMatch(item -> item.contains("ON DUPLICATE KEY UPDATE")));
    }

    @Test
    void shouldBackfillDockerExperienceSchemaForFinanceAndSupplyChainPages() {
        CapturingJdbcTemplate jdbcTemplate = new CapturingJdbcTemplate();
        BpmBundledProcessDefinitionInitializer initializer =
                new BpmBundledProcessDefinitionInitializer(null, null, jdbcTemplate);

        initializer.ensureErpExperienceSchema();

        assertTrue(jdbcTemplate.executedSql.stream()
                .anyMatch(item -> item.contains("CREATE TABLE IF NOT EXISTS `erp_finance_expense`")));
        assertTrue(jdbcTemplate.executedSql.stream()
                .anyMatch(item -> item.contains("CREATE TABLE IF NOT EXISTS `erp_finance_expense_item`")));
        assertTrue(jdbcTemplate.columnChecks.contains(List.of("erp_product", "material_code")));
        assertTrue(jdbcTemplate.columnChecks.contains(List.of("erp_product", "batch_control_flag")));
        assertTrue(jdbcTemplate.columnChecks.contains(List.of("erp_product", "inspection_required_flag")));
        assertTrue(jdbcTemplate.columnChecks.contains(List.of("erp_product", "asset_flag")));
        assertTrue(jdbcTemplate.columnChecks.contains(List.of("erp_stock_in", "process_instance_id")));
        assertTrue(jdbcTemplate.columnChecks.contains(List.of("erp_stock_out", "process_instance_id")));
        assertTrue(jdbcTemplate.columnChecks.contains(List.of("erp_purchase_return", "process_instance_id")));
        assertTrue(jdbcTemplate.columnChecks.contains(List.of("system_dict_data", "biz_attributes")));
        assertTrue(jdbcTemplate.executedSql.stream()
                .anyMatch(item -> item.contains("INSERT INTO `system_dict_type`")
                        && item.contains("erp_expense_type")));
        assertTrue(jdbcTemplate.updatedSql.stream()
                .anyMatch(item -> item.contains("INSERT INTO `system_dict_data`")
                        && item.contains("erp_expense_type")));
    }

    @Test
    void purchaseReturnDefinitionShouldRedeployWhenCandidateStrategyConfigIsMissing() throws Exception {
        Field definitionsField = BpmBundledProcessDefinitionInitializer.class.getDeclaredField("DEFINITIONS");
        definitionsField.setAccessible(true);
        List<?> definitions = (List<?>) definitionsField.get(null);

        Object purchaseReturnDefinition = definitions.stream()
                .filter(definition -> "erp_purchase_return_approval".equals(invokeGetter(definition, "getKey")))
                .findFirst()
                .orElseThrow();

        assertEquals(Boolean.TRUE, invokeGetter(purchaseReturnDefinition, "isRequiresCandidateStrategyCheck"));
    }

    @Test
    void saleOrderDefinitionShouldUseTheSharedNoTenantDeploymentPath() throws Exception {
        Field definitionsField = BpmBundledProcessDefinitionInitializer.class.getDeclaredField("DEFINITIONS");
        definitionsField.setAccessible(true);
        List<?> definitions = (List<?>) definitionsField.get(null);

        Object saleOrderDefinition = definitions.stream()
                .filter(definition -> "erp_sale_order".equals(invokeGetter(definition, "getKey")))
                .findFirst()
                .orElseThrow();

        assertEquals("bpmn/erp_sale_order_approval.bpmn", invokeGetter(saleOrderDefinition, "getClasspath"));
        assertEquals(Boolean.TRUE, invokeGetter(saleOrderDefinition, "isRequiresCandidateStrategyCheck"));
    }

    @Test
    void everyCheckedDefinitionShouldDeclareTaskIdsThatExistInItsBpmnResource() throws Exception {
        Field definitionsField = BpmBundledProcessDefinitionInitializer.class.getDeclaredField("DEFINITIONS");
        definitionsField.setAccessible(true);
        List<?> definitions = (List<?>) definitionsField.get(null);
        Field expectedField = BpmBundledProcessDefinitionInitializer.class.getDeclaredField("EXPECTED_CANDIDATE_PARAMS");
        expectedField.setAccessible(true);
        Map<?, ?> expectedParams = (Map<?, ?>) expectedField.get(null);

        for (Object definition : definitions) {
            if (!Boolean.TRUE.equals(invokeGetter(definition, "isRequiresCandidateStrategyCheck"))) {
                continue;
            }
            String key = (String) invokeGetter(definition, "getKey");
            String classpath = (String) invokeGetter(definition, "getClasspath");
            Set<String> userTaskIds = parseBpmnUserTaskIds(classpath);
            List<String> checkedTaskIds = expectedParams.containsKey(key)
                    ? new ArrayList<String>(((Map<?, ?>) expectedParams.get(key)).keySet()
                            .stream().map(String::valueOf).toList())
                    : List.of("task_approve_level1", "task_approve_level2");
            for (String taskId : checkedTaskIds) {
                assertTrue(userTaskIds.contains(taskId),
                        key + " 的候选策略校验任务 " + taskId + " 不存在于 " + classpath
                                + "（实际 userTask: " + userTaskIds + "），将导致每次启动恒重部署");
            }
        }
    }

    @Test
    void bomDisableDefinitionShouldNotRedeployWhenDeployedModelMatchesExpectation() throws Exception {
        assertFalse(invokeShouldRedeploy(initializerReturningBpmn("bpmn/erp_bom_disable_approval.bpmn"),
                findDefinition("erp_bom_disable_approval")));
        assertFalse(invokeShouldRedeploy(initializerReturningBpmn("bpmn/erp_rd_bom_approval.bpmn"),
                findDefinition("erp_rd_bom_approval")));
    }

    private static BpmBundledProcessDefinitionInitializer initializerReturningBpmn(String classpath) throws Exception {
        BpmnModel deployedModel = BpmnModelUtils.getBpmnModel(readBpmnBytes(classpath));
        return new BpmBundledProcessDefinitionInitializer(
                repositoryServiceReturning(deployedModel), null, new CapturingJdbcTemplate());
    }

    private static byte[] readBpmnBytes(String classpath) throws Exception {
        return new ClassPathResource(classpath).getInputStream().readAllBytes();
    }

    private static Set<String> parseBpmnUserTaskIds(String classpath) throws Exception {
        BpmnModel model = BpmnModelUtils.getBpmnModel(readBpmnBytes(classpath));
        return model.getMainProcess().getFlowElements().stream()
                .filter(UserTask.class::isInstance)
                .map(flowElement -> flowElement.getId())
                .collect(Collectors.toSet());
    }

    private static Object findDefinition(String key) throws Exception {
        Field definitionsField = BpmBundledProcessDefinitionInitializer.class.getDeclaredField("DEFINITIONS");
        definitionsField.setAccessible(true);
        List<?> definitions = (List<?>) definitionsField.get(null);
        return definitions.stream()
                .filter(definition -> key.equals(invokeGetter(definition, "getKey")))
                .findFirst()
                .orElseThrow();
    }

    private static RepositoryService repositoryServiceReturning(BpmnModel model) {
        return (RepositoryService) Proxy.newProxyInstance(RepositoryService.class.getClassLoader(),
                new Class<?>[]{RepositoryService.class},
                (proxy, method, args) -> "getBpmnModel".equals(method.getName()) ? model : null);
    }

    private static boolean invokeShouldRedeploy(BpmBundledProcessDefinitionInitializer initializer,
                                                Object definition) throws Exception {
        Method method = BpmBundledProcessDefinitionInitializer.class.getDeclaredMethod("shouldRedeploy",
                Class.forName(
                        "cn.weitee.erp.module.bpm.service.definition.BpmBundledProcessDefinitionInitializer$BundledProcessDefinition"),
                ProcessDefinition.class);
        method.setAccessible(true);
        return (Boolean) method.invoke(initializer, definition, createProcessDefinition("proxy:1:proxy"));
    }

    private static class CapturingJdbcTemplate extends JdbcTemplate {

        private final List<String> executedSql = new ArrayList<>();
        private final List<String> updatedSql = new ArrayList<>();
        private final List<List<String>> columnChecks = new ArrayList<>();

        @Override
        public void execute(String sql) {
            this.executedSql.add(sql);
        }

        @Override
        public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) {
            columnChecks.add(Arrays.stream(args).map(String::valueOf).toList());
            return requiredType.cast(0);
        }

        @Override
        public int update(String sql, Object... args) {
            this.updatedSql.add(sql);
            return 1;
        }
    }

    private static Object createBundledDefinition(String key, String name, String category,
                                                  String createPath, String viewPath) throws Exception {
        Class<?> type = Class.forName(
                "cn.weitee.erp.module.bpm.service.definition.BpmBundledProcessDefinitionInitializer$BundledProcessDefinition");
        var constructor = type.getDeclaredConstructor(String.class, String.class, String.class, String.class,
                String.class, String.class, boolean.class);
        constructor.setAccessible(true);
        return constructor.newInstance(key, name, "bpmn/" + key + ".bpmn", category, createPath, viewPath, false);
    }

    private static ProcessDefinition createProcessDefinition(String id) {
        return (ProcessDefinition) Proxy.newProxyInstance(ProcessDefinition.class.getClassLoader(),
                new Class<?>[]{ProcessDefinition.class},
                (proxy, method, args) -> {
                    if ("getId".equals(method.getName())) {
                        return id;
                    }
                    if ("toString".equals(method.getName())) {
                        return "ProcessDefinitionProxy";
                    }
                    return null;
                });
    }

    private static Object invokeGetter(Object target, String methodName) {
        try {
            Method method = target.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            return method.invoke(target);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
