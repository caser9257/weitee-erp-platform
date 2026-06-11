package cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.runtime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "管理后台 - 批量审批 Request VO")
@Data
public class BpmApprovalBatchReqVO {

    @Schema(description = "任务ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "任务ID列表不能为空")
    private List<String> taskIds;

    @Schema(description = "审批意见", example = "同意")
    private String comment;

}
