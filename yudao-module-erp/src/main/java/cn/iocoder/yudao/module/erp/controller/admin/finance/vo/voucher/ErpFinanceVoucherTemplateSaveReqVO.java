package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.voucher;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceVoucherAmountSourceEnum;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceVoucherEntryDirectionEnum;
import cn.iocoder.yudao.module.erp.enums.ErpResearchExpenseCategoryEnum;
import cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 财务凭证模板新增/修改 Request VO")
@Data
public class ErpFinanceVoucherTemplateSaveReqVO {

    @Schema(description = "模板编号", example = "1")
    private Long id;

    @Schema(description = "账簿编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "账簿编号不能为空")
    private Long ledgerId;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "40")
    @NotNull(message = "业务类型不能为空")
    @InEnum(ErpBizTypeEnum.class)
    private Integer bizType;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "费用报销标准凭证")
    @NotEmpty(message = "模板名称不能为空")
    private String name;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "是否自动生成", example = "false")
    private Boolean autoGenerate;

    @Schema(description = "默认摘要", example = "费用报销凭证")
    private String defaultSummary;

    @Schema(description = "备注", example = "模块6阶段2")
    private String remark;

    @Schema(description = "研发支出分类", example = "10")
    @InEnum(ErpResearchExpenseCategoryEnum.class)
    private Integer researchCategory;

    @Schema(description = "是否研发专项模板", example = "false")
    private Boolean researchTemplate;

    @Schema(description = "模板分录", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotEmpty(message = "模板分录不能为空")
    private List<Item> items;

    @Schema(description = "管理后台 - 财务凭证模板分录")
    @Data
    public static class Item {

        @Schema(description = "分录编号", example = "1")
        private Long id;

        @Schema(description = "分录方向", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
        @NotNull(message = "分录方向不能为空")
        @InEnum(ErpFinanceVoucherEntryDirectionEnum.class)
        private Integer entryDirection;

        @Schema(description = "科目编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "660201")
        @NotEmpty(message = "科目编码不能为空")
        private String subjectCode;

        @Schema(description = "科目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "管理费用-研发费")
        @NotEmpty(message = "科目名称不能为空")
        private String subjectName;

        @Schema(description = "金额来源", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
        @NotNull(message = "金额来源不能为空")
        @InEnum(ErpFinanceVoucherAmountSourceEnum.class)
        private Integer amountSource;

        @Schema(description = "金额来源值", example = "0.130000")
        private BigDecimal amountSourceValue;

        @Schema(description = "分录摘要", example = "研发报销")
        private String summary;
    }
}
