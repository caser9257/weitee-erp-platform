package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.erp.enums.ErpResearchExpenseCategoryEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 费用报销新增/修改 Request VO")
@Data
public class ErpFinanceExpenseSaveReqVO {

    @Schema(description = "编号", example = "23752")
    private Long id;

    @Schema(description = "报销时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "报销时间不能为空")
    private LocalDateTime expenseTime;

    @Schema(description = "费用类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "费用类型不能为空")
    private Integer expenseType;

    @Schema(description = "研发支出分类（费用化/资本化）", example = "10")
    @InEnum(ErpResearchExpenseCategoryEnum.class)
    private Integer researchCategory;

    @Schema(description = "研发支出口径（10-费用化，20-资本化）", example = "10")
    private Integer rdAccountingType;

    @Schema(description = "部门编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    @NotNull(message = "部门不能为空")
    private Long deptId;

    @Schema(description = "项目编号", example = "1001")
    private Long projectId;

    @Schema(description = "付款对象编号", example = "2001")
    private Long supplierId;

    @Schema(description = "财务人员编号", example = "19690")
    private Long financeUserId;

    @Schema(description = "结算账户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "28989")
    @NotNull(message = "结算账户不能为空")
    private Long accountId;

    @Schema(description = "报销金额，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "10000")
    @NotNull(message = "报销金额不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "报销金额必须大于 0")
    private BigDecimal expensePrice;

    @Schema(description = "备注", example = "零星采购")
    private String remark;

    @Schema(description = "费用明细列表")
    @Valid
    private List<Item> items;

    @Data
    public static class Item {

        @Schema(description = "明细编号", example = "1")
        private Long id;

        @Schema(description = "费用内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "测试材料")
        @NotNull(message = "费用内容不能为空")
        private String itemName;

        @Schema(description = "明细金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "500")
        @NotNull(message = "明细金额不能为空")
        @DecimalMin(value = "0", inclusive = false, message = "明细金额必须大于 0")
        private BigDecimal amount;

        @Schema(description = "明细备注", example = "样机测试采购")
        private String remark;

        @Schema(description = "是否转固定资产候选", example = "true")
        private Boolean assetCandidateFlag;
    }

}
