package cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 生产成本归集明细 Response VO")
@Data
public class ErpProductionCostEntryRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "生产工单编号", example = "1")
    private Long productionOrderId;

    @Schema(description = "生产工单号", example = "SCGD202604280001")
    private String productionOrderNo;

    @Schema(description = "产品编号", example = "1001")
    private Long productId;

    @Schema(description = "产品名称", example = "精密组件A")
    private String productName;

    @Schema(description = "项目编号", example = "2001")
    private Long projectId;

    @Schema(description = "项目号", example = "XM202604280001")
    private String projectNo;

    @Schema(description = "项目名称", example = "新产品项目")
    private String projectName;

    @Schema(description = "成本类型", example = "20")
    private Integer costType;

    @Schema(description = "成本类型名称", example = "直接人工")
    private String costTypeName;

    @Schema(description = "来源类型", example = "10")
    private Integer sourceType;

    @Schema(description = "来源类型名称", example = "手工录入")
    private String sourceTypeName;

    @Schema(description = "归集月份", example = "2026-04")
    private String accountingMonth;

    @Schema(description = "金额", example = "1000.00")
    private BigDecimal amount;

    @Schema(description = "备注", example = "4 月人工费")
    private String remark;

    @Schema(description = "创建人名称", example = "张三")
    private String creatorName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
