package cn.weitee.erp.module.erp.controller.admin.finance.vo.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "管理后台 - ERP 固定资产新增/修改 Request VO")
@Data
public class ErpFinanceAssetSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "资产名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "资产名称不能为空")
    private String name;

    @Schema(description = "资产分类", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "资产分类不能为空")
    private String categoryName;

    @Schema(description = "来源候选记录编号", example = "1")
    private Long candidateId;

    @Schema(description = "来源类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "来源类型不能为空")
    private Integer sourceType;

    @Schema(description = "来源业务编号", example = "1001")
    private Long sourceBizId;

    @Schema(description = "来源业务单号", example = "CGRK20260520000001")
    private String sourceBizNo;

    @Schema(description = "来源明细编号", example = "1")
    private Long sourceItemId;

    @Schema(description = "部门编号", example = "1")
    private Long deptId;

    @Schema(description = "责任人编号", example = "1")
    private Long responsibleUserId;

    @Schema(description = "购置日期")
    private LocalDate purchaseDate;

    @Schema(description = "启用日期")
    private LocalDate startUseDate;

    @Schema(description = "原值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "原值不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "原值必须大于 0")
    private BigDecimal originalAmount;

    @Schema(description = "残值率", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "残值率不能为空")
    @DecimalMin(value = "0", message = "残值率不能小于 0")
    private BigDecimal salvageRate;

    @Schema(description = "折旧方式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "折旧方式不能为空")
    private String depreciationMethod;

    @Schema(description = "折旧月数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "折旧月数不能为空")
    private Integer depreciationPeriodMonths;

    @Schema(description = "折旧起始期间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-05")
    @NotBlank(message = "折旧起始期间不能为空")
    private String depreciationStartPeriod;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "资产类型（0=固定资产, 1=无形资产）", example = "0")
    private Integer assetType;

    @Schema(description = "子分类", example = "专利权")
    private String subCategory;
}
