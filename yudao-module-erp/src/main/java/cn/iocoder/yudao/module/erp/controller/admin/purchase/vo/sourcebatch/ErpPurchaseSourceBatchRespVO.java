package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.sourcebatch;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 采购来源批次 Response VO")
@Data
public class ErpPurchaseSourceBatchRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "来源批次号", requiredMode = Schema.RequiredMode.REQUIRED, example = "CGLY20260427000001")
    private String batchNo;

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long productId;

    @Schema(description = "产品名称", example = "物料A")
    private String productName;

    @Schema(description = "采购订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long purchaseOrderId;

    @Schema(description = "采购订单号", example = "CGDD20260427000001")
    private String purchaseOrderNo;

    @Schema(description = "采购订单明细编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long purchaseOrderItemId;

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long supplierId;

    @Schema(description = "供应商名称", example = "供应商A")
    private String supplierName;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer status;

    @Schema(description = "业务日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate bizDate;

    @Schema(description = "备注", example = "首批采购来源")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;
}
