package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.issuevoucher;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 生产领料出库凭证分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpProductionIssueVoucherPageReqVO extends PageParam {

    @Schema(description = "凭证号", example = "CKPZ202604280001")
    private String voucherNo;

    @Schema(description = "领料单号", example = "SCLL202604280001")
    private String issueNo;

    @Schema(description = "生产工单编号", example = "1")
    private Long productionOrderId;

    @Schema(description = "状态", example = "10")
    private Integer status;

    @Schema(description = "凭证时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] voucherTime;

}
