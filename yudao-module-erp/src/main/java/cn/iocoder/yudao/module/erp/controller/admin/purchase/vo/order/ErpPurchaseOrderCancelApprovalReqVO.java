package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 采购订单撤回审批 Request VO")
@Data
public class ErpPurchaseOrderCancelApprovalReqVO {

    @Schema(description = "采购订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "采购订单编号不能为空")
    private Long id;

    @Schema(description = "撤回原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "资料待补充")
    @NotBlank(message = "撤回原因不能为空")
    private String reason;

}
