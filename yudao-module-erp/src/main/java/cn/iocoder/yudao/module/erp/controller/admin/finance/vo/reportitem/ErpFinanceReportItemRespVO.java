package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.reportitem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 财务报表项目 Response VO")
@Data
public class ErpFinanceReportItemRespVO {

    @Schema(description = "报表项目编号", example = "1")
    private Long id;

    @Schema(description = "账簿编号", example = "1")
    private Long ledgerId;

    @Schema(description = "账簿名称", example = "标准账簿")
    private String ledgerName;

    @Schema(description = "报表类型", example = "10")
    private Integer reportType;

    @Schema(description = "报表类型名称", example = "资产负债表")
    private String reportTypeName;

    @Schema(description = "项目分类", example = "10")
    private Integer itemCategory;

    @Schema(description = "项目分类名称", example = "资产")
    private String itemCategoryName;

    @Schema(description = "项目编码", example = "BS-ASSET")
    private String itemCode;

    @Schema(description = "项目名称", example = "资产合计")
    private String itemName;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "备注", example = "资产负债表项目")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "取数科目映射")
    private List<SubjectMapping> subjects;

    @Schema(description = "管理后台 - ERP 财务报表项目取数科目 Response VO")
    @Data
    public static class SubjectMapping {

        @Schema(description = "映射编号", example = "1")
        private Long id;

        @Schema(description = "科目编码", example = "1002")
        private String subjectCode;

        @Schema(description = "科目名称", example = "银行存款")
        private String subjectName;

        @Schema(description = "取数规则", example = "50")
        private Integer amountRule;

        @Schema(description = "取数规则名称", example = "期末借方")
        private String amountRuleName;

        @Schema(description = "金额符号，1 为加，-1 为减", example = "1")
        private Integer amountSign;
    }
}
