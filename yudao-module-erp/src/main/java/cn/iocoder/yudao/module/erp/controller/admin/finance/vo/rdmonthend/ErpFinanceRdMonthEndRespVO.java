package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.rdmonthend;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 研发月末处理 Response VO")
@Data
public class ErpFinanceRdMonthEndRespVO {

    @Schema(description = "费用化月末结转凭证编号", example = "1001")
    private Long expenseCarryForwardVoucherId;

    @Schema(description = "资本化摊销生成数量", example = "3")
    private Integer capitalizeDepreciationCount;
}
