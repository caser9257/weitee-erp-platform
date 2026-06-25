package cn.weitee.erp.module.erp.controller.admin.finance.vo.expense;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 费用报销明细 Response VO")
@Data
public class ErpFinanceExpenseItemRespVO {

    @Schema(description = "明细编号", example = "1")
    private Long id;

    @Schema(description = "费用内容", example = "测试材料")
    private String itemName;

    @Schema(description = "明细金额", example = "500")
    private BigDecimal amount;

    @Schema(description = "明细备注", example = "样机测试采购")
    private String remark;

    @Schema(description = "是否转固定资产候选", example = "true")
    private Boolean assetCandidateFlag;

}
