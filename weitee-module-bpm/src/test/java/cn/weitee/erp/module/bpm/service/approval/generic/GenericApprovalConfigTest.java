package cn.weitee.erp.module.bpm.service.approval.generic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link GenericApprovalConfig} 的单元测试
 */
class GenericApprovalConfigTest {

    @Test
    public void testParse_normal() {
        GenericApprovalConfig config = GenericApprovalConfig.parse("""
                {
                  "bizTable": "erp_stock_check",
                  "idColumn": "id",
                  "statusColumn": "status",
                  "processInstanceColumn": "process_instance_id",
                  "statusMapping": {"submit": 10, "approve": 20, "reject": 30, "cancel": 0, "failed": 60},
                  "contextFields": {"amount": "total_price", "deptId": "dept_id", "bizNo": "no"}
                }
                """);
        assertEquals("erp_stock_check", config.getBizTable());
        assertEquals("id", config.getIdColumn());
        assertEquals("status", config.getStatusColumn());
        assertEquals("process_instance_id", config.getProcessInstanceColumn());
        assertEquals(10, config.getStatus("submit"));
        assertEquals(20, config.getStatus("approve"));
        assertEquals(30, config.getStatus("reject"));
        assertEquals(0, config.getStatus("cancel"));
        assertEquals(60, config.getStatus("failed"));
        assertEquals("total_price", config.getContextFields().get("amount"));
        assertEquals("dept_id", config.getContextFields().get("deptId"));
    }

    @Test
    public void testParse_blank_returnsNull() {
        assertNull(GenericApprovalConfig.parse(null));
        assertNull(GenericApprovalConfig.parse(""));
        assertNull(GenericApprovalConfig.parse("   "));
    }

    @Test
    public void testParse_missingBizTable_fail() {
        assertThrows(IllegalArgumentException.class,
                () -> GenericApprovalConfig.parse("{\"statusMapping\":{\"submit\":10}}"));
    }

    @Test
    public void testParse_missingSubmitMapping_fail() {
        assertThrows(IllegalArgumentException.class,
                () -> GenericApprovalConfig.parse("{\"bizTable\":\"erp_xxx\"}"));
    }

    @Test
    public void testParse_sqlInjectionIdentifier_fail() {
        // 表名注入尝试：分号/空格/反引号一律拒绝
        assertThrows(IllegalArgumentException.class,
                () -> GenericApprovalConfig.parse("{\"bizTable\":\"erp_xxx; DROP TABLE system_users\"}"));
        assertThrows(IllegalArgumentException.class,
                () -> GenericApprovalConfig.parse("{\"bizTable\":\"erp_xxx\",\"idColumn\":\"id`\"}"));
        assertThrows(IllegalArgumentException.class,
                () -> GenericApprovalConfig.parse("{\"bizTable\":\"erp_xxx\",\"contextFields\":{\"amount\":\"total price\"}}"));
    }

    @Test
    public void testParse_invalidJson_fail() {
        assertThrows(IllegalArgumentException.class,
                () -> GenericApprovalConfig.parse("{ not a json"));
    }

    @Test
    public void testParse_defaults() {
        GenericApprovalConfig config = GenericApprovalConfig.parse(
                "{\"bizTable\":\"erp_xxx\",\"statusMapping\":{\"submit\":10,\"approve\":20}}");
        assertEquals("id", config.getIdColumn());
        assertEquals("status", config.getStatusColumn());
        assertNull(config.getProcessInstanceColumn());
    }

}
