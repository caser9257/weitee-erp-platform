package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.projectdualcost;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 项目双账成本 Response VO")
@Data
public class ErpFinanceDualProjectCostRespVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "项目ID", example = "1")
    private Long projectId;

    @Schema(description = "项目编号", example = "PRJ-2026-001")
    private String projectNo;

    @Schema(description = "项目名称", example = "研发项目A")
    private String projectName;

    @Schema(description = "期间（YYYY-MM）", example = "2026-05")
    private String period;

    @Schema(description = "成本类别", example = "10")
    private Integer costType;

    @Schema(description = "成本类别名称", example = "材料")
    private String costTypeName;

    @Schema(description = "外部账金额", example = "10000.00")
    private BigDecimal externalAmount;

    @Schema(description = "内部账金额", example = "12000.00")
    private BigDecimal internalAmount;

    @Schema(description = "差异金额（内部-外部）", example = "2000.00")
    private BigDecimal diffAmount;

    @Schema(description = "来源单据数", example = "5")
    private Integer sourceCount;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "版本号", example = "1")
    private Integer versionNo;

    @Schema(description = "最后重建时间")
    private LocalDateTime lastRebuildTime;

    @Schema(description = "最后重建人", example = "1")
    private Long lastRebuildBy;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
