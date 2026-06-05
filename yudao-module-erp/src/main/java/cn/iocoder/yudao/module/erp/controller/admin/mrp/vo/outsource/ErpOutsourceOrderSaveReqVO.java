package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.outsource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 委外订单新增/修改 Request VO")
@Data
public class ErpOutsourceOrderSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "委外类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "委外类型不能为空")
    private Integer orderType;

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "供应商不能为空")
    private Long supplierId;

    @Schema(description = "成品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "成品不能为空")
    private Long productId;

    @Schema(description = "BOM 编号", example = "1")
    private Long bomId;

    @Schema(description = "项目编号", example = "1")
    private Long projectId;

    @Schema(description = "工艺/工序名称", example = "喷涂")
    private String processName;

    @Schema(description = "计划数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "计划数量不能为空")
    @DecimalMin(value = "0.000001", message = "计划数量必须大于 0")
    private BigDecimal plannedQty;

    @Schema(description = "备注", example = "喷涂委外")
    private String remark;

}
