package cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost;

import cn.weitee.erp.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP 生产成本归集明细分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpProductionCostEntryPageReqVO extends PageParam {

    @Schema(description = "生产工单编号", example = "1")
    private Long productionOrderId;

    @Schema(description = "成本类型", example = "20")
    private Integer costType;

    @Schema(description = "归集月份", example = "2026-04")
    private String accountingMonth;

}
