package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "管理后台 - ERP MRP 计划新增 Request VO")
@Data
public class ErpMrpPlanSaveReqVO {

    @NotBlank(message = "计划名称不能为空")
    private String planName;

    @NotNull(message = "开始日期不能为空")
    private LocalDate planStartDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate planEndDate;

    private String remark;

}
