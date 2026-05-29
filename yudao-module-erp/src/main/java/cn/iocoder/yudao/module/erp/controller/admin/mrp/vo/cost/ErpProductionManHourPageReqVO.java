package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "管理后台 - ERP 生产工时归集分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpProductionManHourPageReqVO extends PageParam {

    @Schema(description = "生产工单编号", example = "1")
    private Long productionOrderId;

    @Schema(description = "核算月份", example = "2026-04")
    private String accountingMonth;

    @Schema(description = "工时日期范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate[] workDate;

}
