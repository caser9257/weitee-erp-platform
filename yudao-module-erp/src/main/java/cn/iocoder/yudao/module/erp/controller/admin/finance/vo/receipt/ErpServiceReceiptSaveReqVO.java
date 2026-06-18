package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.receipt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "管理后台 - 服务接收单新增/修改 Request VO")
@Data
public class ErpServiceReceiptSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "SR-2026-001")
    @NotEmpty(message = "单号不能为空")
    private String no;

    @Schema(description = "租赁合同ID", example = "1")
    private Long leaseContractId;

    @Schema(description = "供应商ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    @Schema(description = "接收日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "接收日期不能为空")
    private LocalDate receiptDate;

    @Schema(description = "归属期间（YYYY-MM）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-06")
    @NotEmpty(message = "归属期间不能为空")
    private String period;

    @Schema(description = "金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "5000.00")
    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    @Schema(description = "成本中心ID", example = "1")
    private Long costCenterId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "附件URL")
    private String fileUrl;

}
