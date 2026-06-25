package cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost;

import cn.weitee.erp.framework.common.pojo.PageParam;
import cn.weitee.erp.framework.common.validation.InEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostAllocationStatusEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP 生产成本分摊单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpProductionCostAllocationPageReqVO extends PageParam {

    @Schema(description = "分摊单号", example = "CBFT202604280001")
    private String allocationNo;

    @Schema(description = "核算月份", example = "2026-04")
    private String accountingMonth;

    @Schema(description = "成本类型", example = "20")
    @InEnum(value = ErpProductionCostTypeEnum.class, message = "成本类型必须是 {value}")
    private Integer costType;

    @Schema(description = "分摊规则编号", example = "11")
    private Long ruleId;

    @Schema(description = "状态", example = "10")
    @InEnum(value = ErpProductionCostAllocationStatusEnum.class, message = "状态必须是 {value}")
    private Integer status;

}
