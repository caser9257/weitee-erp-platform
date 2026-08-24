package cn.weitee.erp.module.mes.controller.admin.vo.workcalendar;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MesWorkCalendarSaveReqVO {

    private Long id;

    @NotBlank(message = "日历名称不能为空")
    private String calendarName;

    private Long workCenterId;

    private LocalDate effectiveDate;

    private LocalDate expireDate;

    @NotBlank(message = "周几开工不能为空")
    private String weekMask;

    @NotNull(message = "每日可用小时不能为空")
    @DecimalMin(value = "0.5", message = "每日可用小时不能小于 0.5")
    @DecimalMax(value = "24", message = "每日可用小时不能超过 24")
    private BigDecimal dailyHours;

    private String remark;

}
