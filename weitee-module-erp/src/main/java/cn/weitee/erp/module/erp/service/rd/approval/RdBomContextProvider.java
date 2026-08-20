package cn.weitee.erp.module.erp.service.rd.approval;

import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContext;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContextProvider;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomDO;
import cn.weitee.erp.module.erp.dal.mysql.rd.ErpRdBomMapper;
import cn.weitee.erp.module.erp.enums.ErpRdBomBpmConstants;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_NOT_EXISTS;

@Component
public class RdBomContextProvider implements ApprovalContextProvider {

    @Resource
    private ErpRdBomMapper rdBomMapper;

    @Override
    public String getSceneCode() {
        return ErpRdBomBpmConstants.SCENE_CODE;
    }

    @Override
    public ApprovalContext getContext(Long bizId) {
        ErpRdBomDO bom = rdBomMapper.selectById(bizId);
        if (bom == null) {
            throw exception(RD_BOM_NOT_EXISTS);
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put(ErpRdBomBpmConstants.VARIABLE_BOM_ID, bom.getId());
        variables.put(ErpRdBomBpmConstants.VARIABLE_BOM_CODE, bom.getBomCode());
        variables.put(ErpRdBomBpmConstants.VARIABLE_PRODUCT_ID, bom.getProductId());
        Map<String, Object> notifyParams = new HashMap<>();
        notifyParams.put("bizTitle", "研发 BOM " + bom.getBomCode());
        notifyParams.put("bizNo", bom.getBomCode());
        return ApprovalContext.builder()
                .bizId(bom.getId())
                .bizNo(bom.getBomCode())
                .bizTitle("研发 BOM " + bom.getBomCode())
                .startUserId(parseCreatorId(bom.getCreator()))
                .variables(variables)
                .notifyParams(notifyParams)
                .build();
    }

    private Long parseCreatorId(String creator) {
        return creator != null && StrUtil.isNumeric(creator) ? Long.valueOf(creator) : null;
    }

}
