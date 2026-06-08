package cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 销售订单状态更新 Request VO")
@Data
public class ErpSaleOrderUpdateStatusReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    @NotNull(message = "状态不能为空")
    @InEnum(ErpAuditStatus.class)
    private Integer status;

    @Schema(description = "驳回原因", example = "资料不完整")
    private String reason;

}
