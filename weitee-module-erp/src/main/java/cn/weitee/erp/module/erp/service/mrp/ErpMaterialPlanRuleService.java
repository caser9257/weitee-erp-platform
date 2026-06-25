package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.rule.ErpMaterialPlanRulePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.rule.ErpMaterialPlanRuleSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMaterialPlanRuleDO;

import jakarta.validation.Valid;

public interface ErpMaterialPlanRuleService {

    Long createRule(@Valid ErpMaterialPlanRuleSaveReqVO createReqVO);

    void updateRule(@Valid ErpMaterialPlanRuleSaveReqVO updateReqVO);

    void deleteRule(Long id);

    ErpMaterialPlanRuleDO getRule(Long id);

    ErpMaterialPlanRuleDO getRuleByProductId(Long productId);

    PageResult<ErpMaterialPlanRuleDO> getRulePage(ErpMaterialPlanRulePageReqVO pageReqVO);

}
