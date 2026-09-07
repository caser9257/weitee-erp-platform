package cn.weitee.erp.module.erp.controller.admin.rd.vo.bom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 研发 BOM 导入 - 本次导入相较该成品最新版 BOM 的缺失物料差异
 *
 * 仅作增量变更警示（提示不阻断）：用于导入/预检结果中提示
 * "上一版存在而本次文件中缺失的物料"，防止升级改版时漏行。
 * 未对比场景（无法确定成品 / 该成品尚无任何 BOM）返回 null。
 */
@Schema(description = "研发 BOM 导入 - 较最新版缺失物料差异")
@Data
public class ErpRdBomBaselineDiffVO {

    @Schema(description = "基准 BOM 编号（对比所依据的最新版）", example = "1024")
    private Long baselineBomId;

    @Schema(description = "基准 BOM 版本号，未版本化时为 null", example = "V2.0")
    private String baselineVersion;

    @Schema(description = "上一版存在而本次导入缺少的物料清单（按物料去重），无缺失为空数组")
    private List<MissingItem> missingItems;

    @Schema(description = "缺失的单个物料")
    @Data
    public static class MissingItem {

        @Schema(description = "物料编号", example = "MAT-1001")
        private String materialCode;

        @Schema(description = "物料名称", example = "电阻 10K")
        private String productName;
    }

}
