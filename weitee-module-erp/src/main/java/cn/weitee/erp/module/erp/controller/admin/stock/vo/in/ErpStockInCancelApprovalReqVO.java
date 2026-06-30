package cn.weitee.erp.module.erp.controller.admin.stock.vo.in;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 其它入库单取消审批 Request VO")
@Data
public class ErpStockInCancelApprovalReqVO {

    @Schema(description = "其它入库单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "其它入库单编号不能为空")
    private Long id;

    @Schema(description = "撤回原因")
    private String reason;

}
