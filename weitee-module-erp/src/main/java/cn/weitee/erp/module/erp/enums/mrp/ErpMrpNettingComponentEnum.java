package cn.weitee.erp.module.erp.enums.mrp;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum ErpMrpNettingComponentEnum {

    SAFETY_STOCK("SAFETY_STOCK", "安全库存", "DEMAND_ADJUST", 0, true),
    INCOMING_PURCHASE("INCOMING_PURCHASE", "采购在途", "SUPPLY_CONSUME", 10, true),
    WIP_PRODUCTION("WIP_PRODUCTION", "生产在制", "SUPPLY_CONSUME", 20, true),
    ON_HAND_AVAILABLE("ON_HAND_AVAILABLE", "可用库存", "SUPPLY_CONSUME", 30, true);

    private static final Map<String, ErpMrpNettingComponentEnum> COMPONENT_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(ErpMrpNettingComponentEnum::getCode, Function.identity()));

    private final String code;
    private final String label;
    private final String role;
    private final Integer defaultSequence;
    private final boolean defaultEnabled;

    public static ErpMrpNettingComponentEnum valueOfCode(String code) {
        return COMPONENT_MAP.get(code);
    }

    public static List<ErpMrpNettingComponentEnum> configurableComponents() {
        return Arrays.asList(values());
    }
}
