package cn.weitee.erp.module.erp.controller.admin.mrp.vo.outsource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - ERP 委外订单损耗结案 Request VO")
@Data
public class ErpOutsourceOrderCloseReqVO {

    @Schema(description = "委外订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "委外订单编号不能为空")
    private Long orderId;

    @Schema(description = "损耗数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "损耗数量不能为空")
    @DecimalMin(value = "0", message = "损耗数量不能小于 0")
    private BigDecimal lossQty;

    @Schema(description = "结案备注", example = "已确认损耗并结案")
    private String closeRemark;

    @Valid
    @Schema(description = "损耗明细")
    private List<LossItem> lossItems;

    @Schema(description = "管理后台 - ERP 委外订单损耗明细 Request Item")
    @Data
    public static class LossItem {

        @Schema(description = "发料批次编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "发料批次编号不能为空")
        private Long issueBatchId;

        @Schema(description = "损耗数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "损耗数量不能为空")
        @DecimalMin(value = "0.000001", message = "损耗数量必须大于 0")
        private BigDecimal lossQty;

        @Schema(description = "损耗备注", example = "批次损耗 1 PCS")
        private String remark;
    }

}
