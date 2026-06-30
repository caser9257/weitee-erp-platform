package cn.weitee.erp.module.erp.controller.admin.purchase.vo.returns;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 采购退货取消审批 Request VO")
@Data
public class ErpPurchaseReturnCancelApprovalReqVO {

    @Schema(description = "采购退货编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "采购退货编号不能为空")
    private Long id;

    @Schema(description = "撤回原因")
    private String reason;

}
