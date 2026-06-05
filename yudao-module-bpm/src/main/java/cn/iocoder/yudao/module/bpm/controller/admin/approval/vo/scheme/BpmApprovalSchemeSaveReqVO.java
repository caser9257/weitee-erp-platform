package cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.scheme;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 审批方案新增/修改 Request VO")
@Data
public class BpmApprovalSchemeSaveReqVO {

    @Schema(description = "方案编号", example = "1")
    private Long id;

    @Schema(description = "版本编号", example = "10")
    private Long versionId;

    @Schema(description = "方案名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "采购申请审批")
    @NotEmpty(message = "方案名称不能为空")
    private String name;

    @Schema(description = "方案编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "purchase_apply")
    @NotEmpty(message = "方案编码不能为空")
    private String code;

    @Schema(description = "模块编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "erp_purchase")
    @NotEmpty(message = "模块编码不能为空")
    private String moduleCode;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "purchase_apply")
    @NotEmpty(message = "业务类型不能为空")
    private String bizType;

    @Schema(description = "备注", example = "采购申请审批主流程")
    private String remark;

    @Schema(description = "设计器 JSON", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "设计器配置不能为空")
    private String designJson;

    @Schema(description = "通知配置 JSON", example = "{\"notifyType\":\"site_message\"}")
    private String notifyJson;

    @Schema(description = "规则列表")
    @Valid
    private List<Rule> rules;

    @Schema(description = "审批规则")
    @Data
    public static class Rule {

        @Schema(description = "规则名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "默认规则")
        @NotEmpty(message = "规则名称不能为空")
        private String ruleName;

        @Schema(description = "规则类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "DEFAULT")
        @NotEmpty(message = "规则类型不能为空")
        private String ruleType;

        @Schema(description = "优先级", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "优先级不能为空")
        private Integer priority;

        @Schema(description = "是否默认规则", example = "true")
        private Boolean defaultRule;

        @Schema(description = "命中条件 JSON", example = "{}")
        private String conditionJson;

        @Schema(description = "审批流程 JSON", example = "{\"nodes\":[]}")
        private String processJson;

        @Schema(description = "是否启用", example = "true")
        private Boolean enabled;

    }

}
