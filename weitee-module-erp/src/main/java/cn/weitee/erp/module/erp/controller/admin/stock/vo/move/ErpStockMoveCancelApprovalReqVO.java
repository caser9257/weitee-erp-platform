package cn.weitee.erp.module.erp.controller.admin.stock.vo.move;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - ERP 库存调拨单撤回审批 Request VO")
@Data
public class ErpStockMoveCancelApprovalReqVO {

    @NotNull(message = "库存调拨单编号不能为空")
    private Long id;

    private String reason;
}
