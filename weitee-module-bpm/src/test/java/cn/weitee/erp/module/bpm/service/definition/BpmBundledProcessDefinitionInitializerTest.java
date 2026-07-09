package cn.weitee.erp.module.bpm.service.definition;

import org.junit.jupiter.api.Test;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
}
