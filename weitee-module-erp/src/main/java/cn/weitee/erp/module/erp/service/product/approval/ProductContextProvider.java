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

@Component
public class ProductContextProvider implements ApprovalContextProvider {

    @Resource
    private ErpProductMapper productMapper;

    @Override
    public String getSceneCode() {
        return ErpProductBpmConstants.SCENE_CODE;
    }

    @Override
    public ApprovalContext getContext(Long bizId) {
        ErpProductDO product = productMapper.selectById(bizId);
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put(ErpProductBpmConstants.VARIABLE_PRODUCT_ID, product.getId());
        variables.put(ErpProductBpmConstants.VARIABLE_PRODUCT_NAME, product.getName());
        variables.put(ErpProductBpmConstants.VARIABLE_MATERIAL_CODE, product.getMaterialCode());
        Map<String, Object> notifyParams = new HashMap<>();
        notifyParams.put("bizTitle", "物料 " + product.getName());
        notifyParams.put("bizNo", product.getMaterialCode() != null ? product.getMaterialCode() : String.valueOf(product.getId()));
        return ApprovalContext.builder()
                .bizId(product.getId())
                .bizNo(product.getMaterialCode() != null ? product.getMaterialCode() : String.valueOf(product.getId()))
                .bizTitle("物料 " + product.getName())
                .startUserId(parseCreatorId(product.getCreator()))
                .variables(variables)
                .notifyParams(notifyParams)
                .build();
    }

    private Long parseCreatorId(String creator) {
        return creator != null && StrUtil.isNumeric(creator) ? Long.valueOf(creator) : null;
    }

}
