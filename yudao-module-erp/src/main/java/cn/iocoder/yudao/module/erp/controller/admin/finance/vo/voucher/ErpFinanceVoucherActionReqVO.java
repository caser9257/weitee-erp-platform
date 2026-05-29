package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.voucher;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "管理后台 - 财务凭证批量操作 Request VO")
@Data
public class ErpFinanceVoucherActionReqVO {

    @Schema(description = "凭证编号数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1,2]")
    @NotEmpty(message = "凭证编号不能为空")
    private List<Long> ids;

}
