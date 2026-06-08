package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 采购入库确认入库 Request VO")
@Data
public class ErpPurchaseInConfirmStockInReqVO {

    @Schema(description = "采购入库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "采购入库编号不能为空")
    private Long id;

}
