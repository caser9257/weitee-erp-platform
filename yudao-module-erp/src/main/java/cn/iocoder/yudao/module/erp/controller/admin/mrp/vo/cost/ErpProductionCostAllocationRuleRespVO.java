package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 生产成本分摊规则 Response VO")
@Data
public class ErpProductionCostAllocationRuleRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "规则名称", example = "按人工工时")
    private String ruleName;

    @Schema(description = "成本类型", example = "20")
    private Integer costType;

    @Schema(description = "成本类型名称", example = "直接人工")
    private String costTypeName;

    @Schema(description = "分摊基准", example = "10")
    private Integer basisType;

    @Schema(description = "分摊基准名称", example = "人工工时")
    private String basisTypeName;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "默认按人工工时分摊")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
