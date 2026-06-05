package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - ERP 采购入库提交审批 Request VO")
@Data
public class ErpPurchaseInSubmitReqVO {

    @Schema(description = "采购入库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "采购入库编号不能为空")
    private Long id;

    @Schema(description = "发起人自选审批人")
    private Map<String, List<Long>> startUserSelectAssignees;

}
