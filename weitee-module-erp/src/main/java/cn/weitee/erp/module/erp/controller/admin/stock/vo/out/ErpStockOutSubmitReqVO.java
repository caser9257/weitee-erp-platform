package cn.weitee.erp.module.erp.controller.admin.stock.vo.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - ERP 其它出库单提交审批 Request VO")
@Data
public class ErpStockOutSubmitReqVO {

    @Schema(description = "其它出库单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "其它出库单编号不能为空")
    private Long id;

    @Schema(description = "发起人自选审批人")
    private Map<String, List<Long>> startUserSelectAssignees;

}