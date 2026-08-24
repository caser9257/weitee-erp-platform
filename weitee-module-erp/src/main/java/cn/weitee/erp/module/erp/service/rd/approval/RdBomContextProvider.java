package cn.weitee.erp.module.erp.service.rd.approval;

import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContext;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContextProvider;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomApprovalViewRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomDO;
import cn.weitee.erp.module.erp.dal.mysql.rd.ErpRdBomMapper;
import cn.weitee.erp.module.erp.enums.ErpRdBomBpmConstants;
import cn.weitee.erp.module.erp.service.rd.ErpRdBomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.RD_BOM_NOT_EXISTS;

@Component
@Slf4j
public class RdBomContextProvider implements ApprovalContextProvider {

    @Resource
    private ErpRdBomMapper rdBomMapper;
    @Resource
    private ErpRdBomService rdBomService;

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
        fillDiffSummary(variables, bom.getId());
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

    /**
     * 追加差异摘要变量（基准版本 + 新增/删除/修改计数），复用审批视图聚合逻辑保证口径单一。
     * 摘要属于辅助信息，计算失败仅告警，不阻断提交主流程。
     */
    private void fillDiffSummary(Map<String, Object> variables, Long bomId) {
        try {
            ErpRdBomApprovalViewRespVO view = rdBomService.getRdBomApprovalView(bomId);
            boolean firstSubmit = Boolean.TRUE.equals(view.getFirstSubmit());
            variables.put(ErpRdBomBpmConstants.VARIABLE_FIRST_SUBMIT, firstSubmit);
            if (!firstSubmit && view.getDiff() != null) {
                variables.put(ErpRdBomBpmConstants.VARIABLE_BASELINE_VERSION, view.getBaselineVersion());
                variables.put(ErpRdBomBpmConstants.VARIABLE_DIFF_ADDED_COUNT, view.getDiff().getAddedCount());
                variables.put(ErpRdBomBpmConstants.VARIABLE_DIFF_REMOVED_COUNT, view.getDiff().getRemovedCount());
                variables.put(ErpRdBomBpmConstants.VARIABLE_DIFF_CHANGED_COUNT, view.getDiff().getChangedCount());
            }
        } catch (Exception e) {
            log.warn("[fillDiffSummary] 差异摘要计算失败，不阻断提交，bomId={}", bomId, e);
        }
    }

    private Long parseCreatorId(String creator) {
        return creator != null && StrUtil.isNumeric(creator) ? Long.valueOf(creator) : null;
    }

}
