package cn.weitee.erp.module.erp.controller.admin.finance.vo.apstatement;

import cn.weitee.erp.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.weitee.erp.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 应付台账对账 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpApStatementReconciliationReqVO extends PageParam {

    @Schema(description = "供应商编号", example = "1")
    private Long supplierId;

    @Schema(description = "业务类型", example = "11")
    private Integer bizType;

    @Schema(description = "台账编号", example = "AP-11-PI-001")
    private String statementNo;

    @Schema(description = "业务单号", example = "PI-001")
    private String bizNo;

    @Schema(description = "台账状态", example = "10")
    private Integer status;

    @Schema(description = "业务日期范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] bizDate;

}
