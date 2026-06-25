package cn.weitee.erp.module.erp.controller.admin.sale.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 合同导入结果 VO
 *
 * @author system
 */
@Schema(description = "合同导入结果 VO")
@Data
public class ContractImportResultVO {

    @Schema(description = "总行数", example = "100")
    private Integer totalCount;

    @Schema(description = "成功行数", example = "95")
    private Integer successCount;

    @Schema(description = "失败行数", example = "5")
    private Integer failCount;

    @Schema(description = "失败详情")
    private List<FailDetail> failDetails;

    /**
     * 失败详情
     */
    @Data
    public static class FailDetail {

        @Schema(description = "行号", example = "3")
        private Integer rowNumber;

        @Schema(description = "合同编号", example = "CT-2026-001")
        private String contractNo;

        @Schema(description = "失败原因", example = "客户不存在")
        private String reason;

    }

}
