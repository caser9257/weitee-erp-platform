package cn.weitee.erp.module.erp.service.mrp.support;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErpMaterialPlanRuleValidationResult {

    public static final String STATUS_COMPLETE = "COMPLETE";
    public static final String STATUS_INCOMPLETE = "INCOMPLETE";

    private String status;
    private String message;

    public static ErpMaterialPlanRuleValidationResult complete() {
        return new ErpMaterialPlanRuleValidationResult(STATUS_COMPLETE, "规则完整");
    }

    public static ErpMaterialPlanRuleValidationResult incomplete(String message) {
        return new ErpMaterialPlanRuleValidationResult(STATUS_INCOMPLETE, message);
    }

    public boolean isComplete() {
        return STATUS_COMPLETE.equals(status);
    }

}
