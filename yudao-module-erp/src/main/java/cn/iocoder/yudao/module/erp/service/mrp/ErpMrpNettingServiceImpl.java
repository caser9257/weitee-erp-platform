package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpNettingPolicyLineDO;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpMrpNettingComponentEnum;
import cn.iocoder.yudao.module.erp.service.mrp.support.ErpMrpMaterialProjectKey;
import cn.iocoder.yudao.module.erp.service.mrp.support.ErpMrpNettingComponentResult;
import cn.iocoder.yudao.module.erp.service.mrp.support.ErpMrpNettingRequest;
import cn.iocoder.yudao.module.erp.service.mrp.support.ErpMrpNettingResult;
import cn.iocoder.yudao.module.erp.service.mrp.support.ErpMrpNettingRuntimePolicy;
import cn.iocoder.yudao.module.erp.service.mrp.support.ErpMrpSupplyContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ErpMrpNettingServiceImpl implements ErpMrpNettingService {

    @Override
    public ErpMrpNettingResult calculate(ErpMrpNettingRuntimePolicy policy, ErpMrpSupplyContext context,
                                         ErpMrpNettingRequest request) {
        BigDecimal grossDemandQty = defaultDecimal(request.getGrossDemandQty());
        BigDecimal safetyStockQty = defaultDecimal(request.getSafetyStockQty());
        BigDecimal targetQty = grossDemandQty;
        BigDecimal remainingTargetQty = grossDemandQty;
        BigDecimal reservedStockQty = BigDecimal.ZERO;
        List<ErpMrpNettingComponentResult> componentResults = new ArrayList<>();

        componentResults.add(ErpMrpNettingComponentResult.builder()
                .componentCode("GROSS_DEMAND")
                .componentName("毛需求")
                .componentRole("DEMAND_BASE")
                .sequenceNo(0)
                .enableFlag(Boolean.TRUE)
                .baseQty(grossDemandQty)
                .consumedQty(BigDecimal.ZERO)
                .remainingQty(remainingTargetQty)
                .build());

        List<ErpMrpNettingPolicyLineDO> lines = policy.getLines() == null ? List.of() : policy.getLines();
        lines = lines.stream()
                .sorted(Comparator.comparing(ErpMrpNettingPolicyLineDO::getSequenceNo, Comparator.nullsLast(Integer::compareTo)))
                .toList();
        ErpMrpMaterialProjectKey projectKey = new ErpMrpMaterialProjectKey(request.getProjectId(), request.getMaterialId());
        for (ErpMrpNettingPolicyLineDO line : lines) {
            ErpMrpNettingComponentEnum component = ErpMrpNettingComponentEnum.valueOfCode(line.getComponentCode());
            if (component == null || !Boolean.TRUE.equals(line.getEnableFlag())) {
                continue;
            }
            if ("DEMAND_ADJUST".equals(component.getRole())) {
                targetQty = targetQty.add(safetyStockQty);
                remainingTargetQty = targetQty;
                componentResults.add(ErpMrpNettingComponentResult.builder()
                        .componentCode(component.getCode())
                        .componentName(component.getLabel())
                        .componentRole(component.getRole())
                        .sequenceNo(line.getSequenceNo())
                        .enableFlag(Boolean.TRUE)
                        .baseQty(safetyStockQty)
                        .consumedQty(BigDecimal.ZERO)
                        .remainingQty(remainingTargetQty)
                        .build());
                continue;
            }
            BigDecimal baseQty = resolveBaseQty(context, projectKey, request.getMaterialId(), component);
            BigDecimal consumedQty = remainingTargetQty.min(baseQty);
            if (consumedQty.compareTo(BigDecimal.ZERO) > 0) {
                consumeComponent(context, projectKey, request.getMaterialId(), component, consumedQty);
                remainingTargetQty = remainingTargetQty.subtract(consumedQty);
                if (ErpMrpNettingComponentEnum.ON_HAND_AVAILABLE == component) {
                    reservedStockQty = reservedStockQty.add(consumedQty);
                }
            }
            componentResults.add(ErpMrpNettingComponentResult.builder()
                    .componentCode(component.getCode())
                    .componentName(component.getLabel())
                    .componentRole(component.getRole())
                    .sequenceNo(line.getSequenceNo())
                    .enableFlag(Boolean.TRUE)
                    .baseQty(baseQty)
                    .consumedQty(consumedQty)
                    .remainingQty(baseQty.subtract(consumedQty).max(BigDecimal.ZERO))
                    .build());
        }
        return ErpMrpNettingResult.builder()
                .policyCode(policy.getCode())
                .policyVersion(policy.getVersion())
                .targetQty(targetQty)
                .netDemandQty(remainingTargetQty.max(BigDecimal.ZERO))
                .reservedStockQty(reservedStockQty)
                .componentResults(componentResults)
                .build();
    }

    private BigDecimal resolveBaseQty(ErpMrpSupplyContext context, ErpMrpMaterialProjectKey projectKey,
                                      Long materialId, ErpMrpNettingComponentEnum component) {
        if (ErpMrpNettingComponentEnum.INCOMING_PURCHASE == component) {
            return context.getRemainingIncoming(projectKey);
        }
        if (ErpMrpNettingComponentEnum.WIP_PRODUCTION == component) {
            return context.getRemainingWip(projectKey);
        }
        return context.getRemainingStock(materialId);
    }

    private void consumeComponent(ErpMrpSupplyContext context, ErpMrpMaterialProjectKey projectKey,
                                  Long materialId, ErpMrpNettingComponentEnum component, BigDecimal qty) {
        if (ErpMrpNettingComponentEnum.INCOMING_PURCHASE == component) {
            context.consumeIncoming(projectKey, qty);
            return;
        }
        if (ErpMrpNettingComponentEnum.WIP_PRODUCTION == component) {
            context.consumeWip(projectKey, qty);
            return;
        }
        context.consumeStock(materialId, qty);
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
