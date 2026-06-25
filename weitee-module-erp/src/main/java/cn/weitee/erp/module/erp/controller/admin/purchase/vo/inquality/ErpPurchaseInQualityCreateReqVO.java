package cn.weitee.erp.module.erp.controller.admin.purchase.vo.inquality;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 采购入库轻量质检单创建 Request VO")
@Data
public class ErpPurchaseInQualityCreateReqVO {

    @Schema(description = "采购入库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "采购入库编号不能为空")
    private Long purchaseInId;

}
