package cn.weitee.erp.module.erp.controller.admin.finance.vo.match;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 三单匹配 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ErpThreeWayMatchRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "租赁合同ID")
    private Long leaseContractId;

    @Schema(description = "租赁合同编号")
    private String leaseContractNo;

    @Schema(description = "服务接收单ID")
    private Long serviceReceiptId;

    @Schema(description = "服务接收单编号")
    private String serviceReceiptNo;

    @Schema(description = "发票号")
    private String invoiceNo;

    @Schema(description = "发票金额")
    private BigDecimal invoiceAmount;

    @Schema(description = "合同金额")
    private BigDecimal contractAmount;

    @Schema(description = "接收单金额")
    private BigDecimal receiptAmount;

    @Schema(description = "匹配结果：0-不匹配 1-完全匹配 2-部分匹配")
    private Integer matchResult;

    @Schema(description = "匹配说明")
    private String matchRemark;

    @Schema(description = "状态：0-待匹配 10-已确认 20-已生成应付")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
