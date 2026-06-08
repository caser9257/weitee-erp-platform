package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.rule.ErpMaterialPlanRulePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.rule.ErpMaterialPlanRuleSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMaterialPlanRuleDO;

import jakarta.validation.Valid;

public interface ErpMaterialPlanRuleService {

    Long createRule(@Valid ErpMaterialPlanRuleSaveReqVO createReqVO);

    void updateRule(@Valid ErpMaterialPlanRuleSaveReqVO updateReqVO);

    void deleteRule(Long id);

    ErpMaterialPlanRuleDO getRule(Long id);

    ErpMaterialPlanRuleDO getRuleByProductId(Long productId);

    PageResult<ErpMaterialPlanRuleDO> getRulePage(ErpMaterialPlanRulePageReqVO pageReqVO);

}
