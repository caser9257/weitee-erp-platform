package cn.weitee.erp.module.erp.controller.admin.finance.vo.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 固定资产 Response VO")
@Data
public class ErpFinanceAssetRespVO {

    private Long id;
    private String no;
    private String name;
    private String categoryName;
    private Long candidateId;
    private Integer sourceType;
    private Long sourceBizId;
    private String sourceBizNo;
    private Long sourceItemId;
    private Long deptId;
    private Long responsibleUserId;
    private LocalDate purchaseDate;
    private LocalDate startUseDate;
    private BigDecimal originalAmount;
    private BigDecimal salvageRate;
    private BigDecimal salvageAmount;
    private String depreciationMethod;
    private Integer depreciationPeriodMonths;
    private String depreciationStartPeriod;
    private BigDecimal depreciatedAmount;
    private BigDecimal currentAmount;
    private Integer status;
    private String lastDepreciationPeriod;
    private String remark;
    private Integer assetType;
    private String subCategory;
    private String creator;
    private LocalDateTime createTime;
}
