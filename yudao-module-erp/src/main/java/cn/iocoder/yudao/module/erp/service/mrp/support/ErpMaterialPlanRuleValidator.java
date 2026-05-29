package cn.iocoder.yudao.module.erp.service.mrp.support;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMaterialPlanRuleDO;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpMaterialPlanReplenishModeEnum;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpMrpSupplyTypeEnum;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class ErpMaterialPlanRuleValidator {

    public ErpMaterialPlanRuleValidationResult validate(ErpMaterialPlanRuleDO rule) {
        if (rule == null) {
            return ErpMaterialPlanRuleValidationResult.incomplete("未配置计划参数");
        }
        List<String> messages = new ArrayList<>();
        validateSupplyType(rule, messages);
        validateReplenishMode(rule, messages);
        if (isNegative(rule.getSafetyStock())) {
            messages.add("安全库存不能小于 0");
        }
        if (isNegative(rule.getMinOrderQty())) {
            messages.add("最小批量不能小于 0");
        }
        if (rule.getOrderMultiple() == null || rule.getOrderMultiple().compareTo(BigDecimal.ZERO) <= 0) {
            messages.add("倍量必须大于 0");
        }
        if (ErpMrpSupplyTypeEnum.PURCHASE.getType().equals(rule.getSupplyType())
                && (rule.getPurchaseLeadDay() == null || rule.getPurchaseLeadDay() < 0)) {
            messages.add("采购提前期必须大于等于 0");
        }
        if (ErpMrpSupplyTypeEnum.MAKE.getType().equals(rule.getSupplyType())
                && (rule.getMakeLeadDay() == null || rule.getMakeLeadDay() < 0)) {
            messages.add("生产提前期必须大于等于 0");
        }
        if (ErpMaterialPlanReplenishModeEnum.FIXED_LOT.getMode().equals(rule.getReplenishMode())
                && (rule.getFixedOrderQty() == null || rule.getFixedOrderQty().compareTo(BigDecimal.ZERO) <= 0)) {
            messages.add("固定批量必须大于 0");
        }
        if (messages.isEmpty()) {
            return ErpMaterialPlanRuleValidationResult.complete();
        }
        return ErpMaterialPlanRuleValidationResult.incomplete(StrUtil.join("；", messages));
    }

    private void validateSupplyType(ErpMaterialPlanRuleDO rule, List<String> messages) {
        if (StrUtil.isBlank(rule.getSupplyType()) || (!ErpMrpSupplyTypeEnum.PURCHASE.getType().equals(rule.getSupplyType())
                && !ErpMrpSupplyTypeEnum.MAKE.getType().equals(rule.getSupplyType()))) {
            messages.add("供给方式未配置");
        }
    }

    private void validateReplenishMode(ErpMaterialPlanRuleDO rule, List<String> messages) {
        if (StrUtil.isBlank(rule.getReplenishMode())
                || !ErpMaterialPlanReplenishModeEnum.contains(rule.getReplenishMode())) {
            messages.add("补货策略未配置");
        }
    }

    private boolean isNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) < 0;
    }

}
