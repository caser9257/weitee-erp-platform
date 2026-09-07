package cn.weitee.erp.module.erp.controller.admin.product.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 物料修改审批视图 Response VO")
@Data
public class ErpProductApprovalViewRespVO {

    @Schema(description = "物料编号", example = "15672")
    private Long id;

    @Schema(description = "主表当前值")
    private ErpProductRespVO current;

    @Schema(description = "变更后目标值（暂存快照）", nullable = true)
    private ErpProductRespVO target;

    @Schema(description = "字段级变更明细")
    private List<FieldDiff> diffs;

    @Schema(description = "变更字段名清单，逗号分隔", example = "name,purchasePrice")
    private String changedFields;

    @Schema(description = "暂存状态：1待审 2通过 3驳回 4失败；null=无在途变更", example = "1")
    private Integer pendingStatus;

    @Schema(description = "提交说明/审批结果原因")
    private String reason;

    @Schema(description = "流程实例编号")
    private String processInstanceId;

    @Schema(description = "管理后台 - 物料字段级变更")
    @Data
    public static class FieldDiff {

        @Schema(description = "字段名", example = "purchasePrice")
        private String field;

        @Schema(description = "字段中文名", example = "采购价")
        private String label;

        @Schema(description = "变更前值（主表现值）")
        private String oldValue;

        @Schema(description = "变更后值（暂存目标值）")
        private String newValue;

    }

}
