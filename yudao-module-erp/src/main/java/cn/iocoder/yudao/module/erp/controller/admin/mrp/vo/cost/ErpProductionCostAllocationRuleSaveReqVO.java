package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpProductionCostAllocationBasisTypeEnum;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpProductionCostTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 生产成本分摊规则新增/修改 Request VO")
@Data
public class ErpProductionCostAllocationRuleSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "规则名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "按人工工时")
    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    @Schema(description = "成本类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    @NotNull(message = "成本类型不能为空")
    @InEnum(value = ErpProductionCostTypeEnum.class, message = "成本类型必须是 {value}")
    private Integer costType;

    @Schema(description = "分摊基准", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "分摊基准不能为空")
    @InEnum(value = ErpProductionCostAllocationBasisTypeEnum.class, message = "分摊基准必须是 {value}")
    private Integer basisType;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(value = CommonStatusEnum.class, message = "状态必须是 {value}")
    private Integer status;

    @Schema(description = "备注", example = "默认按人工工时分摊")
    private String remark;

}
