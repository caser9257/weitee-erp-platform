package cn.weitee.erp.module.erp.controller.admin.stock.vo.assemble;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 组装拆卸单新增/修改 Request VO")
@Data
public class ErpStockAssembleSaveReqVO {

    @Schema(description = "编号")
    private Long id;

    @NotBlank(message = "作业类型不能为空")
    private String actionType;

    @NotNull(message = "仓库不能为空")
    private Long warehouseId;

    @NotNull(message = "产品不能为空")
    private Long productId;

    @NotNull(message = "数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "数量必须大于 0")
    private BigDecimal count;

    private String remark;
}
