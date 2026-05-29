package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMaterialPlanRuleDO;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpMaterialPlanReplenishModeEnum;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpMrpSupplyTypeEnum;
import cn.iocoder.yudao.module.erp.service.mrp.support.ErpMaterialPlanRuleValidationResult;
import cn.iocoder.yudao.module.erp.service.mrp.support.ErpMaterialPlanRuleValidator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErpMaterialPlanRuleValidatorTest {

    private final ErpMaterialPlanRuleValidator validator = new ErpMaterialPlanRuleValidator();

    @Test
    void validate_shouldReturnCompleteForPurchaseRule() {
        ErpMaterialPlanRuleDO rule = ErpMaterialPlanRuleDO.builder()
                .productId(1000L)
                .supplyType(ErpMrpSupplyTypeEnum.PURCHASE.getType())
                .replenishMode(ErpMaterialPlanReplenishModeEnum.LOT_FOR_LOT.getMode())
                .safetyStock(BigDecimal.ZERO)
                .minOrderQty(BigDecimal.ZERO)
                .orderMultiple(BigDecimal.ONE)
                .purchaseLeadDay(3)
                .enableFlag(Boolean.TRUE)
                .shortageWarnFlag(Boolean.TRUE)
                .build();

        ErpMaterialPlanRuleValidationResult result = validator.validate(rule);

        assertEquals(ErpMaterialPlanRuleValidationResult.STATUS_COMPLETE, result.getStatus());
        assertEquals("规则完整", result.getMessage());
    }

    @Test
    void validate_shouldReturnIncompleteForInvalidFixedLotRule() {
        ErpMaterialPlanRuleDO rule = ErpMaterialPlanRuleDO.builder()
                .productId(1001L)
                .supplyType(ErpMrpSupplyTypeEnum.MAKE.getType())
                .replenishMode(ErpMaterialPlanReplenishModeEnum.FIXED_LOT.getMode())
                .minOrderQty(BigDecimal.ZERO)
                .orderMultiple(BigDecimal.ZERO)
                .fixedOrderQty(BigDecimal.ZERO)
                .makeLeadDay(null)
                .build();

        ErpMaterialPlanRuleValidationResult result = validator.validate(rule);

        assertEquals(ErpMaterialPlanRuleValidationResult.STATUS_INCOMPLETE, result.getStatus());
        assertEquals("倍量必须大于 0；生产提前期必须大于等于 0；固定批量必须大于 0", result.getMessage());
    }

}
