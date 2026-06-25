package cn.weitee.erp.module.erp.controller.admin.finance.vo.apinvoice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 采购发票新增/修改 Request VO")
@Data
public class ErpApInvoiceSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "201")
    @NotNull(message = "供应商编号不能为空")
    private Long supplierId;

    @Schema(description = "发票号", requiredMode = Schema.RequiredMode.REQUIRED, example = "FP-20260428-001")
    @NotBlank(message = "发票号不能为空")
    private String invoiceNo;

    @Schema(description = "发票日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "发票日期不能为空")
    private LocalDateTime invoiceDate;

    @Schema(description = "发票类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "发票类型不能为空")
    private Integer invoiceType;

    @Schema(description = "发票总数量", example = "10")
    @DecimalMin(value = "0", inclusive = false, message = "发票总数量必须大于 0")
    private BigDecimal totalCount;

    @Schema(description = "发票总金额（含税）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.40")
    @NotNull(message = "发票总金额不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "发票总金额必须大于 0")
    private BigDecimal totalAmount;

    @Schema(description = "尾差容忍金额", example = "1")
    @DecimalMin(value = "0", inclusive = true, message = "尾差容忍金额不能小于 0")
    private BigDecimal toleranceAmount;

    @Schema(description = "差异原因", example = "供应商尾差")
    private String differenceReason;

    @Schema(description = "备注", example = "月末集中开票")
    private String remark;

}
