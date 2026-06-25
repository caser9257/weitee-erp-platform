package cn.weitee.erp.module.erp.controller.admin.mrp.vo.outsource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - ERP 委外损耗补录 Request VO")
@Data
public class ErpOutsourceLossEntryCreateReqVO {

    @Schema(description = "委外订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "委外订单编号不能为空")
    private Long orderId;

    @Valid
    @NotEmpty(message = "损耗补录明细不能为空")
    @Schema(description = "损耗补录明细", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Entry> entries;

    @Schema(description = "管理后台 - ERP 委外损耗补录明细 Request Item")
    @Data
    public static class Entry {

        @Schema(description = "发料批次编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "发料批次编号不能为空")
        private Long issueBatchId;

        @Schema(description = "损耗数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "损耗数量不能为空")
        @DecimalMin(value = "0.000001", message = "损耗数量必须大于 0")
        private BigDecimal lossQty;

        @Schema(description = "备注", example = "历史结案补录")
        private String remark;
    }

}
