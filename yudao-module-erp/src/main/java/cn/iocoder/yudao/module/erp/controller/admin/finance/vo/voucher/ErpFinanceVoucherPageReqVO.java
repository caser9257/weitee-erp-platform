package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.voucher;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 财务凭证分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpFinanceVoucherPageReqVO extends PageParam {

    @Schema(description = "账簿编号", example = "1")
    private Long ledgerId;

    @Schema(description = "期间编号", example = "2")
    private Long periodId;

    @Schema(description = "业务类型", example = "40")
    private Integer bizType;

    @Schema(description = "业务单号", example = "LSBX20260429000001")
    private String bizNo;

    @Schema(description = "凭证号", example = "CWPZ20260429000001")
    private String voucherNo;

    @Schema(description = "凭证状态", example = "10")
    private Integer status;

    @Schema(description = "凭证时间")
    private LocalDateTime[] voucherTime;
}
