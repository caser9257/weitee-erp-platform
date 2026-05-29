package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apinvoice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "管理后台 - ERP 采购发票撤销匹配 Request VO")
@Data
public class ErpApInvoiceCancelMatchReqVO {

    @Schema(description = "匹配明细编号列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "匹配明细编号列表不能为空")
    private List<Long> ids;

}
