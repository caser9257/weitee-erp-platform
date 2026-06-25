package cn.weitee.erp.module.erp.controller.admin.finance.vo.receipt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 服务接收单 Response VO")
@Data
public class ErpServiceReceiptRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "单号", example = "SR-2026-001")
    private String no;

    @Schema(description = "租赁合同ID", example = "1")
    private Long leaseContractId;

    @Schema(description = "租赁合同编号", example = "LC-2026-001")
    private String leaseContractNo;

    @Schema(description = "供应商ID", example = "1")
    private Long supplierId;

    @Schema(description = "供应商名称", example = "XX仪器公司")
    private String supplierName;

    @Schema(description = "接收日期")
    private LocalDate receiptDate;

    @Schema(description = "归属期间（YYYY-MM）", example = "2026-06")
    private String period;

    @Schema(description = "金额", example = "5000.00")
    private BigDecimal amount;

    @Schema(description = "成本中心ID", example = "1")
    private Long costCenterId;

    @Schema(description = "成本中心名称", example = "生产部车间")
    private String costCenterName;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "生成的应付台账ID")
    private Long apStatementId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "附件URL")
    private String fileUrl;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
