package cn.weitee.erp.module.erp.controller.admin.product.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 管理后台 - ERP 产品导入结果 VO
 */
@Schema(description = "管理后台 - ERP 产品导入结果 VO")
@Data
public class ErpProductImportResultVO {

    @Schema(description = "总行数", example = "100")
    private Integer totalCount;

    @Schema(description = "成功行数", example = "95")
    private Integer successCount;

    @Schema(description = "失败行数", example = "5")
    private Integer failCount;

    @Schema(description = "失败详情")
    private List<FailDetail> failDetails;

    @Schema(description = "成功行所属分类编号集合（去重）", example = "[1, 2]")
    private List<Long> successCategoryIds;

    /**
     * 失败详情
     */
    @Schema(description = "失败详情")
    @Data
    public static class FailDetail {

        @Schema(description = "Excel 行号", example = "3")
        private Integer rowNumber;

        @Schema(description = "条码", example = "X110")
        private String barCode;

        @Schema(description = "失败原因", example = "条码已存在")
        private String reason;

    }

}
