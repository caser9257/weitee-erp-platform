package cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.scene;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 审批场景 Response VO")
@Data
public class BpmApprovalSceneRespVO {

    @Schema(description = "场景编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "场景编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "erp.finance.payment.submit")
    private String sceneCode;

    @Schema(description = "场景名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "付款单提交审批")
    private String name;

    @Schema(description = "模块编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "erp_finance")
    private String moduleCode;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "payment")
    private String bizType;

    @Schema(description = "动作编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "submit")
    private String actionCode;

    @Schema(description = "当前生效方案编号", example = "1")
    private Long activeSchemeId;

    @Schema(description = "归属用户ID（配置管理员）", example = "1024")
    private Long ownerUserId;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "备注", example = "付款单提交审批场景")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
