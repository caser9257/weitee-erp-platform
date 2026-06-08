package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apinvoice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - ERP 采购发票匹配 Request VO")
@Data
public class ErpApInvoiceMatchReqVO {

    @Schema(description = "采购发票编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "采购发票编号不能为空")
    private Long invoiceId;

    @Schema(description = "差异原因", example = "供应商尾差")
    private String differenceReason;

    @Schema(description = "匹配明细列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "匹配明细列表不能为空")
    @Valid
    private List<Item> items;

    @Data
    public static class Item {

        @Schema(description = "采购入库明细编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
        @NotNull(message = "采购入库明细编号不能为空")
        private Long purchaseInItemId;

        @Schema(description = "匹配数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
        @NotNull(message = "匹配数量不能为空")
        @DecimalMin(value = "0", inclusive = false, message = "匹配数量必须大于 0")
        private BigDecimal matchCount;

        @Schema(description = "匹配金额（含税）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        @NotNull(message = "匹配金额不能为空")
        @DecimalMin(value = "0", inclusive = false, message = "匹配金额必须大于 0")
        private BigDecimal matchAmount;

        @Schema(description = "备注", example = "整单收票")
        private String remark;

    }

}
