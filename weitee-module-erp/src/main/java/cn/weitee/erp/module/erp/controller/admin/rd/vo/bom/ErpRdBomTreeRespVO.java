package cn.weitee.erp.module.erp.controller.admin.rd.vo.bom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - ERP 研发 BOM 结构树 Response VO")
@Data
public class ErpRdBomTreeRespVO {

    @Schema(description = "研发 BOM 编号")
    private Long id;

    @Schema(description = "研发 BOM 编码")
    private String bomCode;

    @Schema(description = "成品编号")
    private Long productId;

    @Schema(description = "成品名称")
    private String productName;

    @Schema(description = "版本")
    private String version;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "层级（根节点为 1）")
    private Integer level;

    @Schema(description = "是否存在下级 BOM")
    private Boolean hasChildrenBom;

    @Schema(description = "下级 BOM 编号")
    private Long childBomId;

    @Schema(description = "下级 BOM 编码")
    private String childBomCode;

    @Schema(description = "下级 BOM 版本")
    private String childBomVersion;

    @Schema(description = "子件列表")
    private List<Item> items;

    @Schema(description = "子件展开后的下级 BOM 子件（树形嵌套）")
    private List<Item> children;

    @Schema(description = "研发 BOM 结构树子件")
    @Data
    public static class Item {

        @Schema(description = "子件明细编号")
        private Long id;

        @Schema(description = "物料编号")
        private Long materialId;

        @Schema(description = "物料名称")
        private String materialName;

        @Schema(description = "物料类型（1=自制/装配体，0=采购件）")
        private Integer materialType;

        @Schema(description = "单位编号")
        private Long unitId;

        @Schema(description = "单位名称")
        private String unitName;

        @Schema(description = "用量")
        private BigDecimal usageQty;

        @Schema(description = "损耗率")
        private BigDecimal lossRate;

        @Schema(description = "位号")
        private String referenceDesignator;

        @Schema(description = "提前期(天)")
        private Integer leadTimeDay;

        @Schema(description = "备注")
        private String remark;

        @Schema(description = "替代料列表")
        private List<Substitute> substitutes;

        @Schema(description = "层级（从 1 开始）")
        private Integer level;

        @Schema(description = "是否存在下级 BOM")
        private Boolean hasChildrenBom;

        @Schema(description = "下级 BOM 编号")
        private Long childBomId;

        @Schema(description = "下级 BOM 编码")
        private String childBomCode;

        @Schema(description = "下级 BOM 版本")
        private String childBomVersion;

        @Schema(description = "下级 BOM 子件")
        private List<Item> children;

        @Schema(description = "研发 BOM 子件替代料")
        @Data
        public static class Substitute {

            @Schema(description = "编号")
            private Long id;

            @Schema(description = "替代物料编号")
            private Long substituteMaterialId;

            @Schema(description = "替代物料名称")
            private String substituteMaterialName;

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
