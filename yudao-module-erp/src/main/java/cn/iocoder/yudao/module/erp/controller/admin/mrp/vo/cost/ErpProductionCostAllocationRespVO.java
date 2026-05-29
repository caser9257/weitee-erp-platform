package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 生产成本分摊单 Response VO")
@Data
public class ErpProductionCostAllocationRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "分摊单号", example = "CBFT202604280001")
    private String allocationNo;

    @Schema(description = "核算月份", example = "2026-04")
    private String accountingMonth;

    @Schema(description = "成本类型", example = "20")
    private Integer costType;

    @Schema(description = "成本类型名称", example = "直接人工")
    private String costTypeName;

    @Schema(description = "分摊规则编号", example = "11")
    private Long ruleId;

    @Schema(description = "分摊规则名称", example = "按人工工时")
    private String ruleName;

    @Schema(description = "待分摊总金额", example = "1000.00")
    private BigDecimal totalAmount;

    @Schema(description = "状态", example = "10")
    private Integer status;

    @Schema(description = "状态名称", example = "草稿")
    private String statusName;

    @Schema(description = "执行时间")
    private LocalDateTime executedTime;

    @Schema(description = "备注", example = "4 月人工分摊")
    private String remark;

    @Schema(description = "创建人", example = "张三")
    private String creatorName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
