package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostAllocationRulePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostAllocationRuleSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionCostAllocationRuleDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionCostAllocationRuleMapper;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostAllocationBasisTypeEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostTypeEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_COST_TYPE_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsMrpExt.PRODUCTION_COST_ALLOCATION_BASIS_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsMrpExt.PRODUCTION_COST_ALLOCATION_RULE_NOT_EXISTS;

@Service
@Validated
public class ErpProductionCostAllocationRuleServiceImpl implements ErpProductionCostAllocationRuleService {

    @Resource
    private ErpProductionCostAllocationRuleMapper erpProductionCostAllocationRuleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProductionCostAllocationRule(ErpProductionCostAllocationRuleSaveReqVO createReqVO) {
        validateRule(createReqVO.getCostType(), createReqVO.getBasisType());
        ErpProductionCostAllocationRuleDO rule = BeanUtils.toBean(createReqVO, ErpProductionCostAllocationRuleDO.class);
        erpProductionCostAllocationRuleMapper.insert(rule);
        return rule.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductionCostAllocationRule(ErpProductionCostAllocationRuleSaveReqVO updateReqVO) {
        validateProductionCostAllocationRuleExists(updateReqVO.getId());
        validateRule(updateReqVO.getCostType(), updateReqVO.getBasisType());
        erpProductionCostAllocationRuleMapper.updateById(BeanUtils.toBean(updateReqVO, ErpProductionCostAllocationRuleDO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProductionCostAllocationRule(Long id) {
        validateProductionCostAllocationRuleExists(id);
        erpProductionCostAllocationRuleMapper.deleteById(id);
    }

    @Override
    public ErpProductionCostAllocationRuleDO getProductionCostAllocationRule(Long id) {
        return erpProductionCostAllocationRuleMapper.selectById(id);
    }

    @Override
    public PageResult<ErpProductionCostAllocationRuleDO> getProductionCostAllocationRulePage(ErpProductionCostAllocationRulePageReqVO pageReqVO) {
        return erpProductionCostAllocationRuleMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpProductionCostAllocationRuleDO> getProductionCostAllocationRuleListByStatus(Integer status) {
        return erpProductionCostAllocationRuleMapper.selectListByStatus(status);
    }

    @Override
    public List<ErpProductionCostAllocationRuleDO> getProductionCostAllocationRuleList(Collection<Long> ids) {
        return erpProductionCostAllocationRuleMapper.selectByIds(ids);
    }

    private void validateRule(Integer costType, Integer basisType) {
        if (!ErpProductionCostTypeEnum.isAllocatableType(costType)) {
            throw exception(PRODUCTION_COST_TYPE_INVALID);
        }
        if (!ErpProductionCostAllocationBasisTypeEnum.isSupported(basisType)) {
            throw exception(PRODUCTION_COST_ALLOCATION_BASIS_INVALID);
        }
    }

    private void validateProductionCostAllocationRuleExists(Long id) {
        if (erpProductionCostAllocationRuleMapper.selectById(id) == null) {
            throw exception(PRODUCTION_COST_ALLOCATION_RULE_NOT_EXISTS);
        }
    }

}
