package cn.weitee.erp.module.erp.controller.admin.finance.vo.apestimate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "管理后台 - ERP 暂估批量操作 Request VO")
@Data
public class ErpApEstimateActionReqVO {

    @Schema(description = "编号数组", example = "[1,2]")
    @NotEmpty(message = "编号不能为空")
    private List<Long> ids;

    @Schema(description = "备注", example = "月结确认")
    private String remark;

}
