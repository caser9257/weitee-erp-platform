package cn.weitee.erp.module.erp.service.mrp.approval;

import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContext;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContextProvider;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpBomMapper;
import cn.weitee.erp.module.erp.enums.ErpBomBpmConstants;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.BOM_NOT_EXISTS;

@Component
public class BomLifecycleContextProvider implements ApprovalContextProvider {

    @Resource
    private ErpBomMapper bomMapper;

    @Override
    public String getSceneCode() {
        return ErpBomBpmConstants.SCENE_CODE_DISABLE;
    }

    @Override
    public ApprovalContext getContext(Long bizId) {
        ErpBomDO bom = bomMapper.selectById(bizId);
        if (bom == null) {
            throw exception(BOM_NOT_EXISTS);
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put(ErpBomBpmConstants.VARIABLE_BOM_ID, bom.getId());
        variables.put(ErpBomBpmConstants.VARIABLE_BOM_CODE, bom.getBomCode());
        variables.put(ErpBomBpmConstants.VARIABLE_PRODUCT_ID, bom.getProductId());
        Map<String, Object> notifyParams = new HashMap<>();
        notifyParams.put("bizTitle", "制造 BOM 停用申请 " + bom.getBomCode());
        notifyParams.put("bizNo", StrUtil.isNotBlank(bom.getBomCode()) ? bom.getBomCode() : String.valueOf(bom.getId()));
        return ApprovalContext.builder()
                .bizId(bom.getId())
                .bizNo(StrUtil.isNotBlank(bom.getBomCode()) ? bom.getBomCode() : String.valueOf(bom.getId()))
                .bizTitle("制造 BOM 停用申请 " + bom.getBomCode())
                .startUserId(parseCreatorId(bom.getCreator()))
                .variables(variables)
                .notifyParams(notifyParams)
                .build();
    }

    private Long parseCreatorId(String creator) {
        return creator != null && StrUtil.isNumeric(creator) ? Long.valueOf(creator) : null;
    }

}
