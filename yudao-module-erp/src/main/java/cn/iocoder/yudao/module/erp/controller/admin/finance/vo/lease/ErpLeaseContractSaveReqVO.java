package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.lease;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "管理后台 - 租赁合同新增/修改 Request VO")
@Data
public class ErpLeaseContractSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "合同编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "LC-2026-001")
    @NotEmpty(message = "合同编号不能为空")
    @Size(max = 64, message = "合同编号长度不能超过 64")
    private String no;

    @Schema(description = "合同名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "检测仪器租赁合同")
    @NotEmpty(message = "合同名称不能为空")
    @Size(max = 200, message = "合同名称长度不能超过 200")
    private String name;

    @Schema(description = "供应商ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    @Schema(description = "开始日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    @Schema(description = "月租金", requiredMode = Schema.RequiredMode.REQUIRED, example = "5000.00")
    @NotNull(message = "月租金不能为空")
    private BigDecimal monthlyRent;

    @Schema(description = "付款周期（月）", example = "1")
    private Integer paymentCycle;

    @Schema(description = "合同总金额", example = "60000.00")
    private BigDecimal totalAmount;

    @Schema(description = "成本中心ID", example = "1")
    private Long costCenterId;

    @Schema(description = "备注", example = "检测设备租赁")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

    @Schema(description = "合同附件URL")
    @Size(max = 500, message = "合同附件URL长度不能超过 500")
    private String fileUrl;

}
