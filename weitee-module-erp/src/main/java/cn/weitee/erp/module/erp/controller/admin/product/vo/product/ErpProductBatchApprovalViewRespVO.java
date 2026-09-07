package cn.weitee.erp.module.erp.controller.admin.product.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 物料批量修改审批视图 Response VO")
@Data
public class ErpProductBatchApprovalViewRespVO {

    @Schema(description = "批次编号", example = "1948236571289630720")
    private Long batchId;

    @Schema(description = "批次状态：1待审 2通过 3驳回 4失败", example = "1")
    private Integer status;

    @Schema(description = "提交说明/审批结果原因")
    private String reason;

    @Schema(description = "流程实例编号")
    private String processInstanceId;

    @Schema(description = "批次内物料变更明细数", example = "225")
    private Integer totalCount;

    @Schema(description = "批次内物料变更明细（按物料编号升序）")
    private List<Item> items;

    @Schema(description = "管理后台 - 物料批量修改审批视图明细项")
    @Data
    public static class Item {

        @Schema(description = "物料编号", example = "15672")
        private Long productId;

        @Schema(description = "物料编号（ERP 编码）", example = "40.02.0773")
        private String materialCode;

        @Schema(description = "物料名称", example = "贴片电阻 10K")
        private String name;

        @Schema(description = "变更字段名清单，逗号分隔", example = "pcbComponent,cadenceSchematicPart")
        private String changedFields;

        @Schema(description = "字段级变更明细")
        private List<ErpProductApprovalViewRespVO.FieldDiff> diffs;

    }

}
