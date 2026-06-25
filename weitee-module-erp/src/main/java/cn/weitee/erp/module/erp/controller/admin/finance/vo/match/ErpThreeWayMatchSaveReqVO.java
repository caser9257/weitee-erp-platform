package cn.weitee.erp.module.erp.controller.admin.finance.vo.match;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 三单匹配执行 Request VO")
@Data
public class ErpThreeWayMatchSaveReqVO {

    @NotNull(message = "租赁合同ID不能为空")
    @Schema(description = "租赁合同ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long leaseContractId;

    @NotNull(message = "服务接收单ID不能为空")
    @Schema(description = "服务接收单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long serviceReceiptId;

    @NotBlank(message = "发票号不能为空")
    @Schema(description = "发票号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String invoiceNo;

}
