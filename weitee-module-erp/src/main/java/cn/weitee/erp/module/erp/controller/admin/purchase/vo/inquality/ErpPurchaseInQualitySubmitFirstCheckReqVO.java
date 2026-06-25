package cn.weitee.erp.module.erp.controller.admin.purchase.vo.inquality;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 采购入库 IQC 初检提交 Request VO")
@Data
public class ErpPurchaseInQualitySubmitFirstCheckReqVO {

    @Schema(description = "质检单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "质检单编号不能为空")
    private Long id;

    @Schema(description = "初检备注", example = "首轮抽检完成")
    private String remark;

    @Schema(description = "初检明细", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotEmpty(message = "初检明细不能为空")
    private List<Item> items;

    @Schema(description = "管理后台 - 采购入库 IQC 初检明细")
    @Data
    public static class Item {

        @Schema(description = "质检单明细编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11")
        @NotNull(message = "质检单明细编号不能为空")
        private Long qualityItemId;

        @Schema(description = "抽检数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
        @NotNull(message = "抽检数量不能为空")
        private BigDecimal sampleCount;

        @Schema(description = "本轮合格数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "8")
        @NotNull(message = "本轮合格数量不能为空")
        private BigDecimal roundPassCount;

        @Schema(description = "本轮不合格数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
        @NotNull(message = "本轮不合格数量不能为空")
        private BigDecimal roundRejectCount;

        @Schema(description = "本轮备注", example = "发现少量外观问题")
        private String roundRemark;

        @Schema(description = "不良原因列表")
        @Valid
        private List<Defect> defects;
    }

    @Schema(description = "管理后台 - 采购入库 IQC 初检不良原因")
    @Data
    public static class Defect {

        @Schema(description = "不良原因编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "不良原因编号不能为空")
        private Long defectReasonId;

        @Schema(description = "不良原因名称", example = "外观不良")
        private String defectReasonName;

        @Schema(description = "不良数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
        @NotNull(message = "不良数量不能为空")
        private BigDecimal defectCount;

        @Schema(description = "不良备注", example = "表面有明显划痕")
        private String defectRemark;
    }

}
