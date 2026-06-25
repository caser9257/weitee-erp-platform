package cn.weitee.erp.module.erp.controller.admin.finance.vo.dualwrite;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 双写日志 Response VO")
@Data
public class ErpFinanceDualWriteLogRespVO {

    @Schema(description = "日志编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "源凭证编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long sourceVoucherId;

    @Schema(description = "源凭证编号", example = "V202401001")
    private String sourceVoucherNo;

    @Schema(description = "目标凭证编号", example = "2")
    private Long targetVoucherId;

    @Schema(description = "目标凭证编号", example = "V202401002")
    private String targetVoucherNo;

    @Schema(description = "源账簿编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long sourceLedgerId;

    @Schema(description = "源账簿名称", example = "对外账簿")
    private String sourceLedgerName;

    @Schema(description = "目标账簿编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Long targetLedgerId;

    @Schema(description = "目标账簿名称", example = "内部账簿")
    private String targetLedgerName;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer bizType;

    @Schema(description = "业务类型名称", example = "采购入库")
    private String bizTypeName;

    @Schema(description = "业务单据编号", example = "1")
    private Long bizId;

    @Schema(description = "双写状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "双写状态名称", example = "成功")
    private String statusName;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "重试次数", example = "0")
    private Integer retryCount;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
