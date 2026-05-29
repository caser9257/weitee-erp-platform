package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.issue;

import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 生产领料单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpProductionIssueRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "领料单号", example = "LY202604270001")
    @ExcelProperty("领料单号")
    private String issueNo;

    @Schema(description = "生产工单编号", example = "1")
    private Long productionOrderId;

    @Schema(description = "生产工单号", example = "PO202604270001")
    @ExcelProperty("生产工单号")
    private String productionOrderNo;

    @Schema(description = "领料时间")
    @ExcelProperty("领料时间")
    private LocalDateTime issueTime;

    @Schema(description = "领料金额", example = "1234.56")
    @ExcelProperty("领料金额")
    private BigDecimal issueAmount;

    @Schema(description = "状态", example = "20")
    private Integer status;

    @Schema(description = "状态名称", example = "已完成")
    @ExcelProperty("状态")
    private String statusName;

    @Schema(description = "备注", example = "生产领料")
    private String remark;

    @Schema(description = "创建人编号", example = "1")
    private String creator;

    @Schema(description = "创建人名称", example = "张三")
    @ExcelProperty("创建人")
    private String creatorName;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "生产领料明细列表")
    private List<Item> items;

    @Data
    public static class Item {

        @Schema(description = "编号", example = "1")
        private Long id;

        @Schema(description = "工单物料编号", example = "1")
        private Long productionMaterialId;

        @Schema(description = "物料编号", example = "1001")
        private Long materialId;

        @Schema(description = "物料名称", example = "螺丝")
        private String materialName;

        @Schema(description = "物料编码", example = "MAT-001")
        private String materialCode;

        @Schema(description = "物料条码", example = "A001")
        private String materialBarCode;

        @Schema(description = "物料单位", example = "个")
        private String productUnitName;

        @Schema(description = "仓库编号", example = "1")
        private Long warehouseId;

        @Schema(description = "仓库名称", example = "原料仓")
        private String warehouseName;

        @Schema(description = "领料数量", example = "10")
        private BigDecimal issueQty;

        @Schema(description = "领料金额", example = "300.00")
        private BigDecimal issueAmount;

        @Schema(description = "备注", example = "车间领料")
        private String remark;

        @Schema(description = "领料批次列表")
        private List<Batch> batches;

    }

    @Data
    public static class Batch {

        @Schema(description = "编号", example = "1")
        private Long id;

        @Schema(description = "库存批次编号", example = "1")
        private Long stockBatchId;

        @Schema(description = "批次号", example = "B202604270001")
        private String batchNo;

        @Schema(description = "入库时间")
        private LocalDateTime inboundTime;

        @Schema(description = "生产日期")
        private LocalDate produceDate;

        @Schema(description = "失效日期")
        private LocalDate expireDate;

        @Schema(description = "领料数量", example = "5")
        private BigDecimal issueQty;

        @Schema(description = "备注", example = "FIFO 领料")
        private String remark;

    }

}
