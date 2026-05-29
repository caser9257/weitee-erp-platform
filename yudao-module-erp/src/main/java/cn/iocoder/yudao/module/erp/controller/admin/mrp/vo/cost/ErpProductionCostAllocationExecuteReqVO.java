package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 生产成本分摊单执行 Request VO")
@Data
public class ErpProductionCostAllocationExecuteReqVO {

    @Schema(description = "分摊单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "分摊单编号不能为空")
    private Long id;

}
