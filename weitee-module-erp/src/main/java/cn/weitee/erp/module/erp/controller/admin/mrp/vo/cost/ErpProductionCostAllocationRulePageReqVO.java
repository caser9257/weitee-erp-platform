package cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.PageParam;
import cn.weitee.erp.framework.common.validation.InEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostAllocationBasisTypeEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP 生产成本分摊规则分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpProductionCostAllocationRulePageReqVO extends PageParam {

    @Schema(description = "规则名称", example = "按人工工时")
    private String ruleName;

    @Schema(description = "成本类型", example = "20")
    @InEnum(value = ErpProductionCostTypeEnum.class, message = "成本类型必须是 {value}")
    private Integer costType;

    @Schema(description = "分摊基准", example = "10")
    @InEnum(value = ErpProductionCostAllocationBasisTypeEnum.class, message = "分摊基准必须是 {value}")
    private Integer basisType;

    @Schema(description = "状态", example = "0")
    @InEnum(value = CommonStatusEnum.class, message = "状态必须是 {value}")
    private Integer status;

}
