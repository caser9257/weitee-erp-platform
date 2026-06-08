package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostAllocationRulePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostAllocationRuleSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionCostAllocationRuleDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

public interface ErpProductionCostAllocationRuleService {

    Long createProductionCostAllocationRule(@Valid ErpProductionCostAllocationRuleSaveReqVO createReqVO);

    void updateProductionCostAllocationRule(@Valid ErpProductionCostAllocationRuleSaveReqVO updateReqVO);

    void deleteProductionCostAllocationRule(Long id);

    ErpProductionCostAllocationRuleDO getProductionCostAllocationRule(Long id);

    PageResult<ErpProductionCostAllocationRuleDO> getProductionCostAllocationRulePage(ErpProductionCostAllocationRulePageReqVO pageReqVO);

    List<ErpProductionCostAllocationRuleDO> getProductionCostAllocationRuleListByStatus(Integer status);

    List<ErpProductionCostAllocationRuleDO> getProductionCostAllocationRuleList(Collection<Long> ids);

}
