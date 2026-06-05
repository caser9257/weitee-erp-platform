package cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - ERP 销售订单提交审批 Request VO")
@Data
public class ErpSaleOrderSubmitReqVO {

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "销售订单编号不能为空")
    private Long id;

    @Schema(description = "发起人自选审批人")
    private Map<String, List<Long>> startUserSelectAssignees;

}
