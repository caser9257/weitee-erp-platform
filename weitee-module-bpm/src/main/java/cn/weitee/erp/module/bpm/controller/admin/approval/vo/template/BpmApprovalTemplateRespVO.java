package cn.weitee.erp.module.bpm.controller.admin.approval.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "管理后台 - 审批模板 Response VO")
@Data
public class BpmApprovalTemplateRespVO {

    @Schema(description = "模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "模板编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "LEAVE_APPROVAL")
    private String code;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "请假审批")
    private String name;

    @Schema(description = "模板分类", example = "OA")
    private String category;

    @Schema(description = "模板图标", example = "ep:calendar")
    private String icon;

    @Schema(description = "模板描述", example = "适用于员工请假申请")
    private String description;

    @Schema(description = "表单配置")
    private Map<String, Object> formConfig;

    @Schema(description = "流程配置")
    private Map<String, Object> flowConfig;

    @Schema(description = "通知配置")
    private Map<String, Object> notifyConfig;

    @Schema(description = "使用次数", example = "100")
    private Long useCount;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "排序", example = "0")
    private Integer sort;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
