package cn.weitee.erp.module.erp.controller.admin.finance.vo.reportitem;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.validation.InEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceReportAmountRuleEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceReportItemCategoryEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceReportTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - ERP 财务报表项目新增/修改 Request VO")
@Data
public class ErpFinanceReportItemSaveReqVO {

    @Schema(description = "报表项目编号", example = "1")
    private Long id;

    @Schema(description = "账簿编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "账簿编号不能为空")
    private Long ledgerId;

    @Schema(description = "报表类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "报表类型不能为空")
    @InEnum(ErpFinanceReportTypeEnum.class)
    private Integer reportType;

    @Schema(description = "项目分类", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "项目分类不能为空")
    @InEnum(ErpFinanceReportItemCategoryEnum.class)
    private Integer itemCategory;

    @Schema(description = "项目编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "BS-ASSET")
    @NotEmpty(message = "项目编码不能为空")
    private String itemCode;

    @Schema(description = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "资产合计")
    @NotEmpty(message = "项目名称不能为空")
    private String itemName;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "备注", example = "资产负债表项目")
    private String remark;

    @Schema(description = "取数科目映射", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotEmpty(message = "取数科目映射不能为空")
    private List<SubjectMapping> subjects;

    @Schema(description = "管理后台 - ERP 财务报表项目取数科目")
    @Data
    public static class SubjectMapping {

        @Schema(description = "科目编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1002")
        @NotEmpty(message = "科目编码不能为空")
        private String subjectCode;

        @Schema(description = "取数规则", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
        @NotNull(message = "取数规则不能为空")
        @InEnum(ErpFinanceReportAmountRuleEnum.class)
        private Integer amountRule;

        @Schema(description = "金额符号，1 为加，-1 为减", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "金额符号不能为空")
        private Integer amountSign;
    }
}
