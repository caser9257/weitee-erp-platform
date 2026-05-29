package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "管理后台 - ERP 固定资产候选确认 Request VO")
@Data
public class ErpFinanceAssetCandidateConfirmReqVO {

    @NotNull(message = "候选记录不能为空")
    private Long candidateId;

    @NotBlank(message = "资产名称不能为空")
    private String name;

    @NotBlank(message = "资产分类不能为空")
    private String categoryName;

    private Long deptId;
    private Long responsibleUserId;
    private LocalDate purchaseDate;
    private LocalDate startUseDate;

    @NotNull(message = "原值不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "原值必须大于 0")
    private BigDecimal originalAmount;

    @NotNull(message = "残值率不能为空")
    @DecimalMin(value = "0", message = "残值率不能小于 0")
    private BigDecimal salvageRate;

    @NotBlank(message = "折旧方式不能为空")
    private String depreciationMethod;

    @NotNull(message = "折旧月数不能为空")
    private Integer depreciationPeriodMonths;

    @NotBlank(message = "折旧起始期间不能为空")
    private String depreciationStartPeriod;

    private String remark;
}
