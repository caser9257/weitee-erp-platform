package cn.weitee.erp.module.erp.controller.admin.mrp.vo.issuevoucher;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 生产领料出库凭证 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpProductionIssueVoucherRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "凭证号", example = "CKPZ202604280001")
    @ExcelProperty("凭证号")
    private String voucherNo;

    @Schema(description = "领料单编号", example = "1")
    private Long issueId;

    @Schema(description = "领料单号", example = "SCLL202604280001")
    @ExcelProperty("领料单号")
    private String issueNo;

    @Schema(description = "生产工单编号", example = "1")
    private Long productionOrderId;

    @Schema(description = "生产工单号", example = "SCGD202604280001")
    @ExcelProperty("生产工单号")
    private String productionOrderNo;

    @Schema(description = "凭证时间")
    @ExcelProperty("凭证时间")
    private LocalDateTime voucherTime;

    @Schema(description = "状态", example = "10")
    private Integer status;

    @Schema(description = "状态名称", example = "已生成")
    @ExcelProperty("状态")
    private String statusName;

    @Schema(description = "总金额", example = "1234.56")
    @ExcelProperty("总金额")
    private BigDecimal totalAmount;

    @Schema(description = "备注", example = "车间领料")
    private String remark;

    @Schema(description = "创建人", example = "张三")
    @ExcelProperty("创建人")
    private String creatorName;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "凭证明细")
    private List<Item> items;

    @Data
    public static class Item {

        @Schema(description = "编号", example = "1")
        private Long id;

        @Schema(description = "领料单明细编号", example = "1")
        private Long issueItemId;

        @Schema(description = "物料编号", example = "1")
        private Long materialId;

        @Schema(description = "物料名称", example = "铝壳")
        private String materialName;

        @Schema(description = "物料编码", example = "MAT-001")
        private String materialCode;

        @Schema(description = "物料条码", example = "BC-001")
        private String materialBarCode;

        @Schema(description = "单位", example = "个")
        private String productUnitName;

        @Schema(description = "仓库编号", example = "1")
        private Long warehouseId;

        @Schema(description = "仓库名称", example = "原料仓")
        private String warehouseName;

        @Schema(description = "领料数量", example = "10")
        private BigDecimal issueQty;

        @Schema(description = "领料金额", example = "300.00")
        private BigDecimal issueAmount;

        @Schema(description = "备注", example = "主料")
        private String remark;

    }

}
