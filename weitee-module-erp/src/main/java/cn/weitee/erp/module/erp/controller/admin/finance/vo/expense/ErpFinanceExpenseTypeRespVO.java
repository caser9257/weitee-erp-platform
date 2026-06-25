package cn.weitee.erp.module.erp.controller.admin.finance.vo.expense;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - ERP 零星报销费用类型 Response VO")
@Data
public class ErpFinanceExpenseTypeRespVO {

    @Schema(description = "类型值", example = "10")
    private Integer value;

    @Schema(description = "类型名称", example = "研发费用")
    private String label;

    @Schema(description = "是否需要项目", example = "true")
    private Boolean projectRequired;

}
