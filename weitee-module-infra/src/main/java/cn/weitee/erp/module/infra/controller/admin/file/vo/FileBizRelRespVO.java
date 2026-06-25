package cn.weitee.erp.module.infra.controller.admin.file.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 文件业务关联 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FileBizRelRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "文件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long fileId;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bizType;

    @Schema(description = "业务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long bizId;

    @Schema(description = "业务单号")
    private String bizNo;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
