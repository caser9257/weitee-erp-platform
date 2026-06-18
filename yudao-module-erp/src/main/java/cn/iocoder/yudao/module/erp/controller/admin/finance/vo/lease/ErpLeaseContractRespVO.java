package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.lease;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 租赁合同 Response VO")
@Data
public class ErpLeaseContractRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "合同编号", example = "LC-2026-001")
    private String no;

    @Schema(description = "合同名称", example = "检测仪器租赁合同")
    private String name;

    @Schema(description = "供应商ID", example = "1")
    private Long supplierId;

    @Schema(description = "供应商名称", example = "XX仪器公司")
    private String supplierName;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "月租金", example = "5000.00")
    private BigDecimal monthlyRent;

    @Schema(description = "付款周期（月）", example = "1")
    private Integer paymentCycle;

    @Schema(description = "合同总金额", example = "60000.00")
    private BigDecimal totalAmount;

    @Schema(description = "成本中心ID", example = "1")
    private Long costCenterId;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "合同附件URL")
    private String fileUrl;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
