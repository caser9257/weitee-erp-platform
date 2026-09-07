package cn.weitee.erp.module.erp.service.product.approval;

import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContext;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContextProvider;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductPendingChangeDO;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductMapper;
import cn.weitee.erp.module.erp.enums.ErpProductBpmConstants;
import cn.weitee.erp.module.erp.service.product.ErpProductPendingChangeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_NOT_EXISTS;

@Component
@Slf4j
public class ProductUpdateContextProvider implements ApprovalContextProvider {

    @Resource
    private ErpProductMapper productMapper;
    @Resource
    private ErpProductPendingChangeService pendingChangeService;

    @Override
    public String getSceneCode() {
        return ErpProductBpmConstants.SCENE_CODE_UPDATE;
    }

    @Override
    public ApprovalContext getContext(Long bizId) {
        ErpProductDO product = productMapper.selectById(bizId);
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put("sceneName", "物料修改审批");
        variables.put(ErpProductBpmConstants.VARIABLE_PRODUCT_ID, product.getId());
        variables.put(ErpProductBpmConstants.VARIABLE_PRODUCT_NAME, product.getName());
        variables.put(ErpProductBpmConstants.VARIABLE_MATERIAL_CODE, product.getMaterialCode());
        fillChangeSummary(variables, bizId);
        Map<String, Object> notifyParams = new HashMap<>();
        notifyParams.put("bizTitle", "物料修改 " + product.getName());
        notifyParams.put("bizNo", StrUtil.isNotBlank(product.getMaterialCode())
                ? product.getMaterialCode() : String.valueOf(product.getId()));
        return ApprovalContext.builder()
                .bizId(product.getId())
                .bizNo(StrUtil.isNotBlank(product.getMaterialCode())
                        ? product.getMaterialCode() : String.valueOf(product.getId()))
                .bizTitle("物料修改 " + product.getName())
                .startUserId(parseCreatorId(product.getCreator()))
                .variables(variables)
                .notifyParams(notifyParams)
                .build();
    }

    /**
     * 追加变更摘要变量（变更字段清单与数量），供流程条件与通知使用。
     * 摘要属于辅助信息，计算失败仅告警，不阻断提交主流程。
     */
    private void fillChangeSummary(Map<String, Object> variables, Long productId) {
        try {
            ErpProductPendingChangeDO pending = pendingChangeService.getPendingChange(productId);
            if (pending != null && StrUtil.isNotBlank(pending.getChangedFields())) {
                variables.put(ErpProductBpmConstants.VARIABLE_CHANGED_FIELDS, pending.getChangedFields());
                variables.put(ErpProductBpmConstants.VARIABLE_CHANGED_COUNT,
                        pending.getChangedFields().split(",").length);
            }
        } catch (Exception e) {
            log.warn("[fillChangeSummary] 变更摘要计算失败，不阻断提交，productId={}", productId, e);
        }
    }

    private Long parseCreatorId(String creator) {
        return creator != null && StrUtil.isNumeric(creator) ? Long.valueOf(creator) : null;
    }

}
