package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.prepayment;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 预付款分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpFinancePrepaymentPageReqVO extends PageParam {

    @Schema(description = "预付款单号", example = "YFK20260427000001")
    private String no;

    @Schema(description = "预付款时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] prepaymentTime;

    @Schema(description = "供应商编号", example = "1")
    private Long supplierId;

    @Schema(description = "创建者", example = "1")
    private String creator;

    @Schema(description = "财务人员编号", example = "1")
    private Long financeUserId;

    @Schema(description = "结算账户编号", example = "1")
    private Long accountId;

    @Schema(description = "状态", example = "10")
    private Integer status;

    @Schema(description = "备注", example = "预付")
    private String remark;

}
