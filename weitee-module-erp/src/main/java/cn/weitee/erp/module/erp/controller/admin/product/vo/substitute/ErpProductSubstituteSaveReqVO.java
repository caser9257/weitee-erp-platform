package cn.weitee.erp.module.erp.controller.admin.product.vo.substitute;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 物料替代料新增/修改 Request VO")
@Data
public class ErpProductSubstituteSaveReqVO {

    @Schema(description = "主物料编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "主物料不能为空")
    private Long productId;

    @Schema(description = "替代料编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "替代料不能为空")
    private Long substituteProductId;

    private Integer priority;

    private BigDecimal replaceRatio;

    private String remark;

}
