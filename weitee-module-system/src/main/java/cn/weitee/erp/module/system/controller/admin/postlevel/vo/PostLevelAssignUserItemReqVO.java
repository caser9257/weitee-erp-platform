package cn.weitee.erp.module.system.controller.admin.postlevel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static cn.weitee.erp.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 岗位分配人员明细 Request VO")
@Data
public class PostLevelAssignUserItemReqVO {

    @NotNull(message = "用户编号不能为空")
    private Long userId;

    @Schema(description = "是否主岗", example = "true")
    private Boolean primary;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startDate;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endDate;

    private String remark;

}
