package cn.weitee.erp.module.erp.controller.admin.finance.vo.voucherfailure;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 凭证生成失败记录 Response VO")
@Data
public class ErpFinanceVoucherFailureRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "业务类型", example = "21")
    private Integer bizType;

    @Schema(description = "业务单据编号", example = "1024")
    private Long bizId;

    @Schema(description = "最近一次失败原因")
    private String errorMessage;

    @Schema(description = "重试次数", example = "0")
    private Integer retryCount;

    @Schema(description = "最近一次重试时间")
    private LocalDateTime lastRetryTime;

    @Schema(description = "状态：0=待重试 1=重试成功 2=人工确认关闭", example = "0")
    private Integer status;

    @Schema(description = "人工确认关闭原因")
    private String confirmReason;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
