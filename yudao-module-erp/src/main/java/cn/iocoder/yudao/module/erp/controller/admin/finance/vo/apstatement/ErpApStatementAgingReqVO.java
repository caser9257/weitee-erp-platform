package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "管理后台 - ERP 应付台账账龄 Request VO")
@Data
public class ErpApStatementAgingReqVO {

    @Schema(description = "供应商编号", example = "1")
    private Long supplierId;

    @Schema(description = "结算账户编号", example = "1")
    private Long accountId;

    @Schema(description = "账龄计算基准日")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate asOfDate;

}
