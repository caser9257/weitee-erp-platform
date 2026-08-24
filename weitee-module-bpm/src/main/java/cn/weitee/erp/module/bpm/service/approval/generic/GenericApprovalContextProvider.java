package cn.weitee.erp.module.bpm.service.approval.generic;

import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContext;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 通用审批上下文提供者（兜底）
 *
 * 为配置了 generic_config 的场景提供审批上下文，无需业务模块编写 Provider。
 * 上下文字段由 generic_config.contextFields 声明：context 字段名 → 业务表列名。
 * 支持：amount / deptId / bizNo / bizId / startUserId / projectId / organId / 自定义变量。
 */
@Component
@Slf4j
public class GenericApprovalContextProvider implements ApprovalContextProvider {

    @Resource
    private GenericBizService genericBizService;

    @Resource
    private GenericApprovalConfigService genericConfigService;

    /** 兜底标识：不会被任何业务场景精确命中（业务场景码为 erp.xxx.submit 格式） */
    public static final String GENERIC_SCENE_CODE = "bpm.generic.approval";

    /** 兜底标识：不会被精确场景命中 */
    @Override
    public String getSceneCode() {
        return GENERIC_SCENE_CODE;
    }

    /**
     * 带场景构建审批上下文
     *
     * @param sceneCode 场景编码
     * @param bizId     业务单据 ID
     * @return 审批上下文；场景未启用通用配置或业务行不存在时返回 null
     */
    public ApprovalContext getContext(String sceneCode, Long bizId) {
        GenericApprovalConfig config = resolveConfig(sceneCode);
        if (config == null) {
            return null;
        }
        Map<String, Object> row = genericBizService.selectRow(config, bizId);
        if (row == null) {
            log.warn("[getContext] 通用审批业务行不存在，sceneCode={}, bizId={}", sceneCode, bizId);
            return null;
        }
        ApprovalContext context = new ApprovalContext();
        context.setBizId(bizId);
        context.setStartUserId(null); // 由调用方在提交时设置，这里保留通用上下文
        // 按 contextFields 映射填充
        config.getContextFields().forEach((field, column) -> {
            Object value = row.get(column);
            if (value == null) {
                return;
            }
            switch (field) {
                case "amount" -> context.setAmount(value instanceof Number
                        ? new BigDecimal(String.valueOf(value)) : null);
                case "deptId" -> context.setDeptId(value instanceof Number
                        ? ((Number) value).longValue() : null);
                case "bizNo" -> context.setBizNo(String.valueOf(value));
                case "bizTitle" -> context.setBizTitle(String.valueOf(value));
                case "projectId" -> context.setProjectId(value instanceof Number
                        ? ((Number) value).longValue() : null);
                case "organId" -> context.setOrganId(value instanceof Number
                        ? ((Number) value).longValue() : null);
                default -> context.getVariables().put(field, value);
            }
        });
        // 默认变量：bizId
        context.getVariables().put("bizId", String.valueOf(bizId));
        return context;
    }

    @Override
    public ApprovalContext getContext(Long bizId) {
        throw new UnsupportedOperationException("通用上下文提供者需通过 getContext(sceneCode, bizId) 调用");
    }

    private GenericApprovalConfig resolveConfig(String sceneCode) {
        return genericConfigService.getConfig(sceneCode);
    }

}
