package cn.iocoder.yudao.module.erp.service.mrp;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMaterialPlanRuleDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpMaterialPlanRuleMapper;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpMaterialPlanReplenishModeEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Service
public class ErpMaterialPlanRuleResolverImpl implements ErpMaterialPlanRuleResolver {

    @Resource
    private ErpMaterialPlanRuleMapper erpMaterialPlanRuleMapper;

    @Override
    public ErpMaterialPlanRuleDO resolveByProductId(Long productId) {
        ErpMaterialPlanRuleDO rule = erpMaterialPlanRuleMapper.selectByProductId(productId);
        if (rule == null) {
            return null;
        }
        if (StrUtil.isBlank(rule.getReplenishMode())) {
            rule.setReplenishMode(ErpMaterialPlanReplenishModeEnum.LOT_FOR_LOT.getMode());
        }
        if (rule.getFixedOrderQty() == null) {
            rule.setFixedOrderQty(BigDecimal.ZERO);
        }
        if (rule.getShortageWarnFlag() == null) {
            rule.setShortageWarnFlag(Boolean.TRUE);
        }
        return rule;
    }

}
