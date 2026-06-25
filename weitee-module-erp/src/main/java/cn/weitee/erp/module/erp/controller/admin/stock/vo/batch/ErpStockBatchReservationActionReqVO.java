package cn.weitee.erp.module.erp.controller.admin.stock.vo.batch;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "管理后台 - ERP 批次预占释放 / 实扣 Request VO")
@Data
public class ErpStockBatchReservationActionReqVO {

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    @NotNull(message = "业务类型不能为空")
    private Integer bizType;

    @Schema(description = "业务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "业务编号不能为空")
    private Long bizId;

    @Schema(description = "备注", example = "销售出库预占释放")
    @Size(max = 255, message = "备注长度不能超过 255 个字符")
    private String remark;

}
