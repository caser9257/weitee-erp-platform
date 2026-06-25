package cn.weitee.erp.module.bpm.controller.admin.approval.vo.scheme;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 审批方案发布 Request VO")
@Data
public class BpmApprovalSchemePublishReqVO {

    @Schema(description = "版本编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "版本编号不能为空")
    private Long versionId;

}
