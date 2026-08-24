package cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.validation.InEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceDiffCalculationTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceDualLedgerDiffItemTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceDualLedgerDiffSourceTypeEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 双账套差异项口径配置新增/修改 Request VO")
@Data
public class ErpFinanceDualLedgerDiffConfigSaveReqVO {

    @Schema(description = "配置编号", example = "1")
    private Long id;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "11")
    @NotNull(message = "业务类型不能为空")
    @InEnum(ErpBizTypeEnum.class)
    private Integer bizType;

    @Schema(description = "差异项类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    @NotNull(message = "差异项类型不能为空")
    @InEnum(ErpFinanceDualLedgerDiffItemTypeEnum.class)
    private Integer diffItemType;

    @Schema(description = "对外账来源类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "对外账来源类型不能为空")
    @InEnum(ErpFinanceDualLedgerDiffSourceTypeEnum.class)
    private Integer externalSourceType;

    @Schema(description = "对外账来源值", example = "20")
    private Integer externalSourceValue;

    @Schema(description = "内部账来源类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    @NotNull(message = "内部账来源类型不能为空")
    @InEnum(ErpFinanceDualLedgerDiffSourceTypeEnum.class)
    private Integer internalSourceType;

    @Schema(description = "内部账来源值", example = "60")
    private Integer internalSourceValue;

    @Schema(description = "计算类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "计算类型不能为空")
    @InEnum(ErpFinanceDiffCalculationTypeEnum.class)
    private Integer calculationType;

    @Schema(description = "比例系数（必须大于 1）", example = "1.20")
    private BigDecimal ratio;

    @Schema(description = "固定差额（必须小于 0）", example = "-20.00")
    private BigDecimal fixedAmount;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "备注", example = "人工成本对外账按直接人工，内部账按制造费用吸收")
    private String remark;

}
