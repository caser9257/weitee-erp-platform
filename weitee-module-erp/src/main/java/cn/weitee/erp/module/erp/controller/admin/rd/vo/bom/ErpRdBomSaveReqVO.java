package cn.weitee.erp.module.erp.controller.admin.rd.vo.bom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - ERP 研发 BOM 新增/修改 Request VO")
@Data
public class ErpRdBomSaveReqVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "BOM 编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "BOM 编码不能为空")
    private String bomCode;

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "产品编号不能为空")
    private Long productId;

    @Schema(description = "版本")
    private String version;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "子件列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "子件列表不能为空")
    private List<Item> items;

    @Data
    public static class Item {

        @Schema(description = "编号")
        private Long id;

        @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "物料编号不能为空")
        private Long materialId;

        @Schema(description = "产品型号快照")
        private String materialStandard;

        @Schema(description = "物料类型")
        private Integer materialType;

        @Schema(description = "单位编号")
        private Long unitId;

        @Schema(description = "用量", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "用量不能为空")
        private BigDecimal usageQty;

        @Schema(description = "损耗率")
        private BigDecimal lossRate;

        @Schema(description = "位号（如 R1,C2,U3）")
        private String referenceDesignator;

        @Schema(description = "物料位置（安装位置）")
        private String position;

        @Schema(description = "提前期")
        private Integer leadTimeDay;

        @Schema(description = "排序")
        private Integer sort;

        @Schema(description = "备注")
        private String remark;

        @Schema(description = "替代料列表")
        @Valid
        private List<Substitute> substitutes = List.of();

        @Data
        public static class Substitute {

            @Schema(description = "编号")
            private Long id;

            @Schema(description = "替代物料编号", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "替代物料编号不能为空")
            private Long substituteMaterialId;

            @Schema(description = "优先级")
            private Integer priority;

            @Schema(description = "替换比例")
            private BigDecimal replaceRatio;

            @Schema(description = "是否自动推荐")
            private Boolean enableAutoRecommend;

            @Schema(description = "排序")
            private Integer sort;

            @Schema(description = "备注")
            private String remark;

        }


    }

}
