package cn.weitee.erp.module.erp.service.product.approval;

import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContext;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContextProvider;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductMapper;
import cn.weitee.erp.module.erp.enums.ErpProductBpmConstants;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_NOT_EXISTS;

/**
 * 产品两段式变更/废除上下文提供者基类：
 * 四个场景（变更阶段一/二、废除阶段一/二）的上下文结构一致，仅场景码与标题前缀不同。
 */
public abstract class AbstractProductTwoStageContextProvider implements ApprovalContextProvider {

    @Resource
    private ErpProductMapper productMapper;

    /**
     * 审批标题（如「物料变更申请」「物料废除确认」）
     */
    protected abstract String bizTitlePrefix();

    @Override
    public ApprovalContext getContext(Long bizId) {
        ErpProductDO product = productMapper.selectById(bizId);
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        Map<String, Object> variables = new HashMap<>();
        // sceneName：业务动作名，注入流程实例名标题模板（{sceneName}-{materialCode}-{startTime}）
        variables.put("sceneName", bizTitlePrefix());
        variables.put(ErpProductBpmConstants.VARIABLE_PRODUCT_ID, product.getId());
        variables.put(ErpProductBpmConstants.VARIABLE_PRODUCT_NAME, product.getName());
        variables.put(ErpProductBpmConstants.VARIABLE_MATERIAL_CODE, product.getMaterialCode());
        Map<String, Object> notifyParams = new HashMap<>();
        notifyParams.put("bizTitle", bizTitlePrefix() + " " + product.getName());
        notifyParams.put("bizNo", StrUtil.isNotBlank(product.getMaterialCode())
                ? product.getMaterialCode() : String.valueOf(product.getId()));
        return ApprovalContext.builder()
                .bizId(product.getId())
                .bizNo(StrUtil.isNotBlank(product.getMaterialCode())
                        ? product.getMaterialCode() : String.valueOf(product.getId()))
                .bizTitle(bizTitlePrefix() + " " + product.getName())
                .startUserId(parseCreatorId(product.getCreator()))
                .variables(variables)
                .notifyParams(notifyParams)
                .build();
    }

    private Long parseCreatorId(String creator) {
        return creator != null && StrUtil.isNumeric(creator) ? Long.valueOf(creator) : null;
    }

    @Component
    public static class ChangeRequest extends AbstractProductTwoStageContextProvider {
        @Override
        protected String bizTitlePrefix() {
            return "物料变更申请";
        }

        @Override
        public String getSceneCode() {
            return ErpProductBpmConstants.SCENE_CODE_CHANGE_REQUEST;
        }
    }

    @Component
    public static class ChangeConfirm extends AbstractProductTwoStageContextProvider {
        @Override
        protected String bizTitlePrefix() {
            return "物料变更完成确认";
        }

        @Override
        public String getSceneCode() {
            return ErpProductBpmConstants.SCENE_CODE_CHANGE_CONFIRM;
        }
    }

    @Component
    public static class ObsoleteRequest extends AbstractProductTwoStageContextProvider {
        @Override
        protected String bizTitlePrefix() {
            return "物料废除申请";
        }

        @Override
        public String getSceneCode() {
            return ErpProductBpmConstants.SCENE_CODE_OBSOLETE_REQUEST;
        }
    }

    @Component
    public static class StatusChange extends AbstractProductTwoStageContextProvider {
        @Override
        protected String bizTitlePrefix() {
            return "物料启停审批";
        }

        @Override
        public String getSceneCode() {
            return ErpProductBpmConstants.SCENE_CODE_STATUS_CHANGE;
        }
    }

}
