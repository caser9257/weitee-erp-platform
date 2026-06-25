package cn.weitee.erp.module.erp.controller.admin.purchase.vo.in;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - ERP 采购入库质检 Request VO")
@Data
public class ErpPurchaseInQualityCheckReqVO {

    @Schema(description = "采购入库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "采购入库编号不能为空")
    private Long id;

    @Schema(description = "质检备注", example = "来料外观合格")
    private String remark;

    @Schema(description = "质检明细", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotNull(message = "质检明细不能为空")
    private List<Item> items;

    @Data
    public static class Item {

        @Schema(description = "入库明细编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "入库明细编号不能为空")
        private Long id;

        @Schema(description = "合格数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
        @NotNull(message = "合格数量不能为空")
        private BigDecimal qaPassCount;

        @Schema(description = "不合格数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
        @NotNull(message = "不合格数量不能为空")
        private BigDecimal qaRejectCount;

        @Schema(description = "明细质检备注", example = "1件外观瑕疵")
        private String qaRemark;

    }

}
