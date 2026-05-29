package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.rule.ErpMaterialPlanRulePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.rule.ErpMaterialPlanRuleSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMaterialPlanRuleDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpMaterialPlanRuleMapper;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpMaterialPlanReplenishModeEnum;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpMrpSupplyTypeEnum;
import cn.iocoder.yudao.module.erp.service.mrp.support.ErpMaterialPlanRuleValidationResult;
import cn.iocoder.yudao.module.erp.service.mrp.support.ErpMaterialPlanRuleValidator;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpSupplierService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.MATERIAL_PLAN_RULE_NOT_EXISTS;

@Service
@Validated
public class ErpMaterialPlanRuleServiceImpl implements ErpMaterialPlanRuleService {

    @Resource
    private ErpMaterialPlanRuleMapper erpMaterialPlanRuleMapper;
    @Resource
    private ErpMaterialPlanRuleResolver materialPlanRuleResolver;
    @Resource
    private ErpMaterialPlanRuleValidator materialPlanRuleValidator;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpSupplierService supplierService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRule(ErpMaterialPlanRuleSaveReqVO createReqVO) {
        ErpMaterialPlanRuleDO rule = buildRule(createReqVO);
        validateRuleReq(rule);
        ErpMaterialPlanRuleDO existed = erpMaterialPlanRuleMapper.selectByProductId(rule.getProductId());
        if (existed != null) {
            erpMaterialPlanRuleMapper.updateById(rule.setId(existed.getId()));
            return existed.getId();
        }
        erpMaterialPlanRuleMapper.insert(rule);
        return rule.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRule(ErpMaterialPlanRuleSaveReqVO updateReqVO) {
        validateRuleExists(updateReqVO.getId());
        ErpMaterialPlanRuleDO rule = buildRule(updateReqVO).setId(updateReqVO.getId());
        validateRuleReq(rule);
        erpMaterialPlanRuleMapper.updateById(rule);
    }

    @Override
    public void deleteRule(Long id) {
        validateRuleExists(id);
        erpMaterialPlanRuleMapper.deleteById(id);
    }

    @Override
    public ErpMaterialPlanRuleDO getRule(Long id) {
        return erpMaterialPlanRuleMapper.selectById(id);
    }

    @Override
    public ErpMaterialPlanRuleDO getRuleByProductId(Long productId) {
        return materialPlanRuleResolver.resolveByProductId(productId);
    }

    @Override
    public PageResult<ErpMaterialPlanRuleDO> getRulePage(ErpMaterialPlanRulePageReqVO pageReqVO) {
        return erpMaterialPlanRuleMapper.selectPage(pageReqVO);
    }

    private void validateRuleReq(ErpMaterialPlanRuleDO rule) {
        productService.validProductList(List.of(rule.getProductId()));
        if (rule.getDefaultSupplierId() != null) {
            supplierService.validateSupplier(rule.getDefaultSupplierId());
        }
        ErpMaterialPlanRuleValidationResult validationResult = materialPlanRuleValidator.validate(rule);
        if (!validationResult.isComplete()) {
            throw invalidParamException(validationResult.getMessage());
        }
    }

    private ErpMaterialPlanRuleDO buildRule(ErpMaterialPlanRuleSaveReqVO reqVO) {
        ErpMaterialPlanRuleDO rule = BeanUtils.toBean(reqVO, ErpMaterialPlanRuleDO.class);
        if (rule.getEnableFlag() == null) {
            rule.setEnableFlag(Boolean.TRUE);
        }
        if (rule.getShortageWarnFlag() == null) {
            rule.setShortageWarnFlag(Boolean.TRUE);
        }
        if (rule.getSafetyStock() == null) {
            rule.setSafetyStock(BigDecimal.ZERO);
        }
        if (rule.getMinOrderQty() == null) {
            rule.setMinOrderQty(BigDecimal.ZERO);
        }
        if (rule.getOrderMultiple() == null) {
            rule.setOrderMultiple(BigDecimal.ONE);
        }
        if (rule.getFixedOrderQty() == null) {
            rule.setFixedOrderQty(BigDecimal.ZERO);
        }
        if (ErpMrpSupplyTypeEnum.PURCHASE.getType().equals(rule.getSupplyType())) {
            rule.setMakeLeadDay(null);
        }
        if (ErpMrpSupplyTypeEnum.MAKE.getType().equals(rule.getSupplyType())) {
            rule.setPurchaseLeadDay(null);
            rule.setDefaultSupplierId(null);
        }
        if (!ErpMaterialPlanReplenishModeEnum.FIXED_LOT.getMode().equals(rule.getReplenishMode())) {
            rule.setFixedOrderQty(BigDecimal.ZERO);
        }
        return rule;
    }

    private ErpMaterialPlanRuleDO validateRuleExists(Long id) {
        ErpMaterialPlanRuleDO rule = erpMaterialPlanRuleMapper.selectById(id);
        if (rule == null) {
            throw exception(MATERIAL_PLAN_RULE_NOT_EXISTS);
        }
        return rule;
    }

}
