package cn.weitee.erp.module.bpm.service.approval.generic;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 通用审批接入配置
 *
 * 配置驱动的新单据接入 BPM 审批，无需编写 Java 三件套。
 * 存储于审批场景的 generic_config 字段，JSON 格式：
 * <pre>
 * {
 *   "bizTable": "erp_stock_check",
 *   "idColumn": "id",
 *   "statusColumn": "status",
 *   "processInstanceColumn": "process_instance_id",
 *   "statusMapping": { "submit": 10, "approve": 20, "reject": 30, "cancel": 0, "failed": 60 },
 *   "contextFields": { "amount": "total_price", "deptId": "dept_id", "bizNo": "no" }
 * }
 * </pre>
 *
 * 安全约束：bizTable / 列名仅允许 [A-Za-z_][A-Za-z0-9_]*，值一律参数绑定，防止 SQL 注入。
 */
@Data
public class GenericApprovalConfig {

    public static final String KEY_SUBMIT = "submit";
    public static final String KEY_APPROVE = "approve";
    public static final String KEY_REJECT = "reject";
    public static final String KEY_CANCEL = "cancel";
    public static final String KEY_FAILED = "failed";

    /** 业务表名（必填） */
    private String bizTable;
    /** 主键列名，默认 id */
    private String idColumn = "id";
    /** 状态列名，默认 status */
    private String statusColumn = "status";
    /** 流程实例ID列名（可选），提交后回写 BPM processInstanceId */
    private String processInstanceColumn;
    /** 状态映射：submit/approve/reject/cancel/failed → 业务状态值 */
    private Map<String, Integer> statusMapping = new HashMap<>();
    /** 上下文字段映射：审批上下文变量名 → 业务表列名（amount/deptId/bizNo 等） */
    private Map<String, String> contextFields = new HashMap<>();

    /**
     * 解析并校验配置
     *
     * @param json generic_config JSON；为空返回 null（表示未启用通用接入）
     */
    public static GenericApprovalConfig parse(String json) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        JSONObject obj;
        try {
            obj = JSONUtil.parseObj(json);
        } catch (Exception e) {
            throw new IllegalArgumentException("通用审批配置 JSON 格式非法: " + json, e);
        }
        GenericApprovalConfig config = new GenericApprovalConfig();
        config.setBizTable(StrUtil.trimToNull(obj.getStr("bizTable")));
        config.setIdColumn(StrUtil.blankToDefault(obj.getStr("idColumn"), "id"));
        config.setStatusColumn(StrUtil.blankToDefault(obj.getStr("statusColumn"), "status"));
        config.setProcessInstanceColumn(StrUtil.trimToNull(obj.getStr("processInstanceColumn")));
        if (obj.getJSONObject("statusMapping") != null) {
            obj.getJSONObject("statusMapping").forEach((k, v) -> {
                if (v instanceof Number) {
                    config.getStatusMapping().put(k, ((Number) v).intValue());
                }
            });
        }
        if (obj.getJSONObject("contextFields") != null) {
            obj.getJSONObject("contextFields").forEach((k, v) -> {
                if (v != null) {
                    config.getContextFields().put(k, String.valueOf(v));
                }
            });
        }
        config.validate();
        return config;
    }

    public void validate() {
        if (StrUtil.isBlank(bizTable)) {
            throw new IllegalArgumentException("通用审批配置缺少 bizTable（业务表名）");
        }
        validateIdentifier(bizTable, "bizTable");
        validateIdentifier(idColumn, "idColumn");
        validateIdentifier(statusColumn, "statusColumn");
        if (processInstanceColumn != null) {
            validateIdentifier(processInstanceColumn, "processInstanceColumn");
        }
        contextFields.forEach((k, v) -> validateIdentifier(v, "contextFields." + k));
        if (getStatus(KEY_SUBMIT) == null) {
            throw new IllegalArgumentException("通用审批配置缺少 statusMapping.submit");
        }
    }

    /** 获取指定语义的状态值（submit/approve/reject/cancel/failed） */
    public Integer getStatus(String key) {
        return statusMapping.get(key);
    }

    private static void validateIdentifier(String name, String field) {
        if (!name.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("通用审批配置字段(" + field + ") 非法，仅允许字母数字下划线: " + name);
        }
    }

    public boolean equalsStatus(Integer actual, String key) {
        return ObjectUtil.equal(actual, getStatus(key));
    }
}
