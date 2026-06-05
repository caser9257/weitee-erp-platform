package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.prepayment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "管理后台 - ERP 预付款核销回滚 Request VO")
@Data
public class ErpFinancePrepaymentRollbackReqVO {

    @Schema(description = "核销记录编号列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "核销记录编号列表不能为空")
    private List<Long> ids;

    @Schema(description = "回滚备注", example = "预付冲应付回滚")
    private String remark;

}
