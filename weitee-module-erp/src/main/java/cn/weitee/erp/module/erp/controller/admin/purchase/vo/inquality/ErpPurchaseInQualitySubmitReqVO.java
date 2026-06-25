package cn.weitee.erp.module.erp.controller.admin.purchase.vo.inquality;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 采购入库轻量质检单提交 Request VO")
@Data
public class ErpPurchaseInQualitySubmitReqVO {

    @Schema(description = "质检单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "质检单编号不能为空")
    private Long id;

    @Schema(description = "质检备注", example = "抽检完成")
    private String remark;

    @Schema(description = "质检明细", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotNull(message = "质检明细不能为空")
    private List<Item> items;

    @Data
    public static class Item {

        @Schema(description = "质检单明细编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "质检单明细编号不能为空")
        private Long id;

        @Schema(description = "合格数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
        @NotNull(message = "合格数量不能为空")
        private BigDecimal qaPassCount;

        @Schema(description = "不合格数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "不合格数量不能为空")
        private BigDecimal qaRejectCount;

        @Schema(description = "质检备注", example = "外观破损")
        private String qaRemark;

    }

}
