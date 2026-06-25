package cn.weitee.erp.module.bpm.controller.admin.approval.vo.scheme;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 审批方案 Response VO")
@Data
public class BpmApprovalSchemeRespVO {

    @Schema(description = "方案编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "方案编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "purchase_apply")
    private String code;

    @Schema(description = "方案名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "采购申请审批")
    private String name;

    @Schema(description = "模块编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "erp_purchase")
    private String moduleCode;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "purchase_apply")
    private String bizType;

    @Schema(description = "备注", example = "采购审批主流程")
    private String remark;

    @Schema(description = "当前生效版本编号", example = "10")
    private Long activeVersionId;

    @Schema(description = "最新版本编号", example = "11")
    private Long latestVersionId;

    @Schema(description = "最新版本号", example = "2")
    private Integer latestVersionNo;

    @Schema(description = "最新版本状态", example = "30")
    private Integer latestVersionStatus;

    @Schema(description = "设计器 JSON")
    private String designJson;

    @Schema(description = "规则列表")
    private List<Rule> rules;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "审批规则")
    @Data
    public static class Rule {

        @Schema(description = "规则编号", example = "1")
        private Long id;

        @Schema(description = "规则名称", example = "默认规则")
        private String ruleName;

        @Schema(description = "规则类型", example = "DEFAULT")
        private String ruleType;

        @Schema(description = "优先级", example = "1")
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
