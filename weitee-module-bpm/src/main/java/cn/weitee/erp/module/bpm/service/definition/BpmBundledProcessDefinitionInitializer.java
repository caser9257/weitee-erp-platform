package cn.weitee.erp.module.bpm.service.definition;

import cn.weitee.erp.module.bpm.dal.mysql.definition.BpmProcessDefinitionInfoMapper;
import cn.weitee.erp.module.bpm.enums.definition.BpmModelFormTypeEnum;
import cn.weitee.erp.module.bpm.enums.definition.BpmModelTypeEnum;
import cn.weitee.erp.module.bpm.framework.flowable.core.enums.BpmnModelConstants;
import cn.weitee.erp.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 部署内置的财务、供应链审批 BPMN，保证 Docker 空库启动后可直接提交审批。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "weitee.bpm.bundled-process-definition", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class BpmBundledProcessDefinitionInitializer implements ApplicationRunner {

    private static final List<BundledProcessDefinition> DEFINITIONS = List.of(
            new BundledProcessDefinition("erp_finance_payment", "付款单审批", "bpmn/erp_finance_payment.bpmn",
                    "finance", "/finance/payment", "/finance/payment", false),
            new BundledProcessDefinition("erp_finance_expense", "费用报销审批", "bpmn/erp_finance_expense.bpmn",
                    "finance", "/finance/expense", "/finance/expense", false),
            new BundledProcessDefinition("erp_purchase_return_approval", "采购退货审批",
                    "bpmn/erp_purchase_return_approval.bpmn", "erp_purchase",
                    "/scm/purchase-return", "/scm/purchase-return", false),
            new BundledProcessDefinition("erp_stock_in_approval", "其它入库审批",
                    "bpmn/erp_stock_in_approval.bpmn", "erp_stock",
                    "/scm/stock-in", "/scm/stock-in", true),
            new BundledProcessDefinition("erp_stock_out_approval", "其它出库审批",
                    "bpmn/erp_stock_out_approval.bpmn", "erp_stock",
                    "/scm/stock-out", "/scm/stock-out", true)
    );

    private final RepositoryService repositoryService;
    private final BpmProcessDefinitionInfoMapper processDefinitionInfoMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) throws Exception {
        ensureProcessDefinitionInfoSchema();
        ensureErpExperienceSchema();
        for (BundledProcessDefinition definition : DEFINITIONS) {
            ProcessDefinition activeDefinition = getLatestActiveDefinition(definition.getKey());
            if (activeDefinition == null || shouldRedeploy(definition, activeDefinition)) {
                activeDefinition = deploy(definition);
            }
            ensureDefinitionInfo(definition, activeDefinition);
        }
    }

    private ProcessDefinition getLatestActiveDefinition(String key) {
        return repositoryService.createProcessDefinitionQuery()
                .processDefinitionTenantId(ProcessEngineConfiguration.NO_TENANT_ID)
                .processDefinitionKey(key)
                .active()
                .orderByProcessDefinitionVersion()
                .desc()
                .listPage(0, 1)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private boolean shouldRedeploy(BundledProcessDefinition definition, ProcessDefinition activeDefinition) {
        if (!definition.isRequiresCandidateStrategyCheck()) {
            return false;
        }
        BpmnModel deployedModel = repositoryService.getBpmnModel(activeDefinition.getId());
        return !hasCandidateStrategy(deployedModel, "task_approve_level1")
                || !hasCandidateStrategy(deployedModel, "task_approve_level2");
    }

    private boolean hasCandidateStrategy(BpmnModel model, String taskId) {
        if (model == null) {
            return false;
        }
        FlowElement task = BpmnModelUtils.getFlowElementById(model, taskId);
        return task != null
                && BpmnModelUtils.parseCandidateStrategy(task) != null
                && BpmnModelUtils.parseCandidateParam(task) != null
                && BpmnModelUtils.parseAssignStartUserHandlerType(task) != null
                && BpmnModelUtils.parseAssignEmptyHandlerType(task) != null;
    }

    private ProcessDefinition deploy(BundledProcessDefinition definition) throws Exception {
        ClassPathResource resource = new ClassPathResource(definition.getClasspath());
        byte[] bpmnBytes = resource.getContentAsString(StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8);
        Deployment deployment = repositoryService.createDeployment()
                .key(definition.getKey())
                .name(definition.getName())
                .category(definition.getCategory())
                .addBytes(definition.getKey() + BpmnModelConstants.BPMN_FILE_SUFFIX, bpmnBytes)
                .tenantId(ProcessEngineConfiguration.NO_TENANT_ID)
                .disableSchemaValidation()
                .deploy();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();
        repositoryService.setProcessDefinitionCategory(processDefinition.getId(), definition.getCategory());
        log.info("[deploy][内置 BPMN 部署完成，key={}, definitionId={}, deploymentId={}]",
                definition.getKey(), processDefinition.getId(), deployment.getId());
        return processDefinition;
    }

    void ensureProcessDefinitionInfoSchema() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS `bpm_process_definition_info` (
                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
                  `process_definition_id` varchar(128) NOT NULL COMMENT '流程定义编号',
                  `model_id` varchar(128) DEFAULT NULL COMMENT '流程模型编号',
                  `model_type` int DEFAULT NULL COMMENT '流程模型类型',
                  `category` varchar(64) DEFAULT NULL COMMENT '流程分类',
                  `icon` varchar(255) DEFAULT NULL COMMENT '图标',
                  `description` varchar(512) DEFAULT NULL COMMENT '描述',
                  `form_type` int DEFAULT NULL COMMENT '表单类型',
                  `form_id` bigint DEFAULT NULL COMMENT '动态表单编号',
                  `form_conf` mediumtext DEFAULT NULL COMMENT '表单配置',
                  `form_fields` mediumtext DEFAULT NULL COMMENT '表单字段',
                  `form_custom_create_path` varchar(255) DEFAULT NULL COMMENT '自定义表单提交路径',
                  `form_custom_view_path` varchar(255) DEFAULT NULL COMMENT '自定义表单查看路径',
                  `simple_model` mediumtext DEFAULT NULL COMMENT '简单模型数据',
                  `visible` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否可见',
                  `sort` bigint NOT NULL DEFAULT 0 COMMENT '排序值',
                  `start_user_ids` varchar(512) DEFAULT NULL COMMENT '可发起用户编号',
                  `start_dept_ids` varchar(512) DEFAULT NULL COMMENT '可发起部门编号',
                  `manager_user_ids` varchar(512) DEFAULT NULL COMMENT '可管理用户编号',
                  `allow_cancel_running_process` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否允许撤销审批中的申请',
                  `allow_withdraw_task` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否允许审批人撤回任务',
                  `process_id_rule` mediumtext DEFAULT NULL COMMENT '流程 ID 规则',
                  `auto_approval_type` int DEFAULT NULL COMMENT '自动去重类型',
                  `title_setting` mediumtext DEFAULT NULL COMMENT '标题设置',
                  `summary_setting` mediumtext DEFAULT NULL COMMENT '摘要设置',
                  `process_before_trigger_setting` mediumtext DEFAULT NULL COMMENT '流程前置通知设置',
                  `process_after_trigger_setting` mediumtext DEFAULT NULL COMMENT '流程后置通知设置',
                  `task_before_trigger_setting` mediumtext DEFAULT NULL COMMENT '任务前置通知设置',
                  `task_after_trigger_setting` mediumtext DEFAULT NULL COMMENT '任务后置通知设置',
                  `notification_policy_setting` mediumtext DEFAULT NULL COMMENT '通知策略设置',
                  `print_template_setting` mediumtext DEFAULT NULL COMMENT '自定义打印模板设置',
                  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                  PRIMARY KEY (`id`),
                  UNIQUE KEY `uk_bpm_process_definition_info_definition` (`process_definition_id`),
                  KEY `idx_bpm_process_definition_info_model` (`model_id`),
                  KEY `idx_bpm_process_definition_info_category` (`category`)
                ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'BPM 流程定义扩展信息'
                """);
        addColumnIfMissing("bpm_process_definition_info", "notification_policy_setting",
                "`notification_policy_setting` mediumtext DEFAULT NULL COMMENT '通知策略设置' AFTER `task_after_trigger_setting`");
        addColumnIfMissing("bpm_process_definition_info", "print_template_setting",
                "`print_template_setting` mediumtext DEFAULT NULL COMMENT '自定义打印模板设置' AFTER `notification_policy_setting`");
    }

    void ensureErpExperienceSchema() {
        ensureProductExperienceSchema();
        ensureFinanceExpenseSchema();
        ensureSupplyChainBpmSchema();
        ensureExpenseTypeSchema();
    }

    private void ensureProductExperienceSchema() {
        addColumnIfMissing("erp_product", "material_code",
                "`material_code` varchar(64) DEFAULT NULL COMMENT '物料编码' AFTER `name`");
        addColumnIfMissing("erp_product", "batch_control_flag",
                "`batch_control_flag` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否批次管理' AFTER `expiry_day`");
        addColumnIfMissing("erp_product", "inspection_required_flag",
                "`inspection_required_flag` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否来料检验' AFTER `batch_control_flag`");
        addColumnIfMissing("erp_product", "asset_flag",
                "`asset_flag` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否固定资产候选' AFTER `min_price`");
    }

    private void ensureFinanceExpenseSchema() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS `erp_finance_expense` (
                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                  `no` varchar(64) NOT NULL COMMENT 'expense no',
                  `status` int NOT NULL DEFAULT 10 COMMENT 'expense status',
                  `process_instance_id` varchar(64) DEFAULT NULL COMMENT 'BPM流程实例编号',
                  `expense_time` datetime NOT NULL COMMENT 'expense time',
                  `expense_type` int NOT NULL COMMENT 'expense type',
                  `research_category` int DEFAULT NULL COMMENT '研发支出分类',
                  `rd_accounting_type` int DEFAULT NULL COMMENT '研发支出口径：10-费用化，20-资本化',
                  `dept_id` bigint NOT NULL COMMENT 'dept id',
                  `project_id` bigint DEFAULT NULL COMMENT 'project id',
                  `supplier_id` bigint DEFAULT NULL COMMENT '付款对象编号',
                  `finance_user_id` bigint DEFAULT NULL COMMENT 'finance user id',
                  `account_id` bigint NOT NULL COMMENT 'account id',
                  `expense_price` decimal(24,6) NOT NULL COMMENT 'expense price',
                  `paid_price` decimal(24,6) NOT NULL DEFAULT 0 COMMENT 'paid price',
                  `remain_price` decimal(24,6) NOT NULL DEFAULT 0 COMMENT 'remain price',
                  `remark` varchar(255) DEFAULT NULL COMMENT 'remark',
                  `lease_contract_no` varchar(128) DEFAULT NULL COMMENT '租赁合同编号',
                  `creator` varchar(64) NOT NULL DEFAULT '' COMMENT 'creator',
                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
                  `updater` varchar(64) NOT NULL DEFAULT '' COMMENT 'updater',
                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
                  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
                  PRIMARY KEY (`id`),
                  UNIQUE KEY `uk_finance_expense_no` (`no`, `deleted`),
                  KEY `idx_erp_finance_expense_process_instance_id` (`process_instance_id`),
                  KEY `idx_finance_expense_time` (`expense_time`, `deleted`),
                  KEY `idx_finance_expense_dept_status` (`dept_id`, `status`, `deleted`),
                  KEY `idx_finance_expense_project_status` (`project_id`, `status`, `deleted`),
                  KEY `idx_finance_expense_type_status` (`expense_type`, `status`, `deleted`),
                  KEY `idx_finance_expense_supplier_status` (`supplier_id`, `status`, `deleted`)
                ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'ERP finance expense'
                """);
        addColumnIfMissing("erp_finance_expense", "process_instance_id",
                "`process_instance_id` varchar(64) DEFAULT NULL COMMENT 'BPM流程实例编号' AFTER `status`");
        addColumnIfMissing("erp_finance_expense", "research_category",
                "`research_category` int DEFAULT NULL COMMENT '研发支出分类' AFTER `expense_type`");
        addColumnIfMissing("erp_finance_expense", "rd_accounting_type",
                "`rd_accounting_type` int DEFAULT NULL COMMENT '研发支出口径：10-费用化，20-资本化' AFTER `research_category`");
        addColumnIfMissing("erp_finance_expense", "supplier_id",
                "`supplier_id` bigint DEFAULT NULL COMMENT '付款对象编号' AFTER `project_id`");
        addColumnIfMissing("erp_finance_expense", "lease_contract_no",
                "`lease_contract_no` varchar(128) DEFAULT NULL COMMENT '租赁合同编号' AFTER `remark`");
        addIndexIfMissing("erp_finance_expense", "idx_erp_finance_expense_process_instance_id",
                "ALTER TABLE `erp_finance_expense` ADD INDEX `idx_erp_finance_expense_process_instance_id` (`process_instance_id`)");

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS `erp_finance_expense_item` (
                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                  `expense_id` bigint NOT NULL COMMENT '费用单编号',
                  `item_name` varchar(128) NOT NULL COMMENT '费用内容',
                  `amount` decimal(24,6) NOT NULL COMMENT '金额',
                  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
                  `asset_candidate_flag` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否转固定资产候选',
                  `creator` varchar(64) NOT NULL DEFAULT '' COMMENT '创建者',
                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                  `updater` varchar(64) NOT NULL DEFAULT '' COMMENT '更新者',
                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                  PRIMARY KEY (`id`),
                  KEY `idx_finance_expense_item_expense` (`expense_id`, `deleted`)
                ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'ERP 费用报销明细'
                """);
        addColumnIfMissing("erp_finance_expense_item", "asset_candidate_flag",
                "`asset_candidate_flag` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否转固定资产候选' AFTER `remark`");
    }

    private void ensureSupplyChainBpmSchema() {
        addColumnIfMissing("erp_purchase_return", "process_instance_id",
                "`process_instance_id` varchar(64) DEFAULT NULL COMMENT 'BPM 流程实例 ID' AFTER `status`");
        addColumnIfMissing("erp_stock_in", "process_instance_id",
                "`process_instance_id` varchar(64) DEFAULT NULL COMMENT 'BPM 流程实例 ID' AFTER `status`");
        addColumnIfMissing("erp_stock_out", "process_instance_id",
                "`process_instance_id` varchar(64) DEFAULT NULL COMMENT 'BPM 流程实例 ID' AFTER `status`");
    }

    private void ensureExpenseTypeSchema() {
        addColumnIfMissing("system_dict_data", "biz_attributes",
                "`biz_attributes` json DEFAULT NULL COMMENT '业务属性JSON' AFTER `remark`");
        jdbcTemplate.execute("""
                INSERT INTO `system_dict_type`
                    (`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
                SELECT '费用类型', 'erp_expense_type', 0, 'ERP费用报销类型配置', '1', NOW(), '1', NOW(), b'0'
                WHERE NOT EXISTS (
                  SELECT 1 FROM `system_dict_type`
                  WHERE `type` = 'erp_expense_type' AND `deleted` = b'0'
                )
                """);
        seedExpenseType("研发费用", "10", 10,
                "{\"core\":true,\"projectRequired\":true,\"autoGenerateVoucher\":true,\"voucherBizType\":41,\"category\":\"RD\"}");
        seedExpenseType("差旅费", "20", 20,
                "{\"core\":true,\"projectRequired\":false,\"category\":\"OTHER\"}");
        seedExpenseType("材料费", "30", 30,
                "{\"core\":true,\"projectRequired\":false,\"category\":\"OTHER\"}");
        seedExpenseType("测试费", "40", 40,
                "{\"core\":true,\"projectRequired\":false,\"category\":\"OTHER\"}");
        seedExpenseType("招待费", "50", 50,
                "{\"core\":true,\"projectRequired\":false,\"category\":\"OTHER\"}");
        seedExpenseType("服务费", "60", 60,
                "{\"core\":true,\"projectRequired\":false,\"category\":\"OTHER\"}");
        seedExpenseType("人工费", "70", 70,
                "{\"core\":true,\"projectRequired\":false,\"category\":\"OTHER\"}");
        seedExpenseType("零星采购", "80", 80,
                "{\"core\":true,\"projectRequired\":false,\"assetCandidateFlag\":true,\"category\":\"OTHER\"}");
        seedExpenseType("其他", "90", 90,
                "{\"core\":true,\"projectRequired\":false,\"category\":\"OTHER\"}");
        seedExpenseType("房租费", "100", 100,
                "{\"core\":false,\"projectRequired\":false,\"costCenterRequired\":true,\"category\":\"ADMIN\"}");
        seedExpenseType("仪器租赁费", "200", 200,
                "{\"core\":false,\"projectRequired\":false,\"leaseContractRequired\":true,\"category\":\"LEASE\"}");
    }

    private void seedExpenseType(String label, String value, int sort, String bizAttributes) {
        jdbcTemplate.update("""
                INSERT INTO `system_dict_data`
                    (`dict_type`, `label`, `value`, `sort`, `status`, `color_type`, `css_class`, `remark`,
                     `biz_attributes`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
                SELECT 'erp_expense_type', ?, ?, ?, 0, '', '', '系统内置费用类型', ?,
                       '1', NOW(), '1', NOW(), b'0'
                WHERE NOT EXISTS (
                  SELECT 1 FROM `system_dict_data`
                  WHERE `dict_type` = 'erp_expense_type'
                    AND `value` = ?
                    AND `deleted` = b'0'
                )
                """, label, value, sort, bizAttributes, value);
    }

    private void addColumnIfMissing(String tableName, String columnName, String columnDefinition) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                """, Integer.class, tableName, columnName);
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE `" + tableName + "` ADD COLUMN " + columnDefinition);
    }

    private void addIndexIfMissing(String tableName, String indexName, String ddl) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND INDEX_NAME = ?
                """, Integer.class, tableName, indexName);
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.execute(ddl);
    }

    private void ensureDefinitionInfo(BundledProcessDefinition definition, ProcessDefinition processDefinition) {
        String modelId = "bundled-" + definition.getKey();
        long sort = System.currentTimeMillis();
        int updated = jdbcTemplate.update("""
                UPDATE `bpm_process_definition_info`
                SET
                    `model_id` = ?,
                    `model_type` = ?,
                    `category` = ?,
                    `description` = ?,
                    `form_type` = ?,
                    `form_custom_create_path` = ?,
                    `form_custom_view_path` = ?,
                    `visible` = b'1',
                    `allow_cancel_running_process` = b'1',
                    `allow_withdraw_task` = b'1',
                    `updater` = '1',
                    `update_time` = NOW(),
                    `deleted` = b'0'
                WHERE `process_definition_id` = ?
                """,
                modelId,
                BpmModelTypeEnum.BPMN.getType(),
                definition.getCategory(),
                definition.getName(),
                BpmModelFormTypeEnum.CUSTOM.getType(),
                definition.getFormCustomCreatePath(),
                definition.getFormCustomViewPath(),
                processDefinition.getId());
        if (updated == 0) {
            jdbcTemplate.update("""
                    INSERT INTO `bpm_process_definition_info`
                        (`process_definition_id`, `model_id`, `model_type`, `category`, `description`, `form_type`,
                         `form_custom_create_path`, `form_custom_view_path`, `visible`, `sort`,
                         `allow_cancel_running_process`, `allow_withdraw_task`,
                         `creator`, `create_time`, `updater`, `update_time`, `deleted`)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, b'1', ?, b'1', b'1', '1', NOW(), '1', NOW(), b'0')
                    """,
                    processDefinition.getId(),
                    modelId,
                    BpmModelTypeEnum.BPMN.getType(),
                    definition.getCategory(),
                    definition.getName(),
                    BpmModelFormTypeEnum.CUSTOM.getType(),
                    definition.getFormCustomCreatePath(),
                    definition.getFormCustomViewPath(),
                    sort);
        }
        log.info("[ensureDefinitionInfo][内置 BPMN 扩展信息已补齐，key={}, definitionId={}]",
                definition.getKey(), processDefinition.getId());
    }

    @Getter
    @AllArgsConstructor
    private static class BundledProcessDefinition {

        private final String key;
        private final String name;
        private final String classpath;
        private final String category;
        private final String formCustomCreatePath;
        private final String formCustomViewPath;
        private final boolean requiresCandidateStrategyCheck;
    }
}
