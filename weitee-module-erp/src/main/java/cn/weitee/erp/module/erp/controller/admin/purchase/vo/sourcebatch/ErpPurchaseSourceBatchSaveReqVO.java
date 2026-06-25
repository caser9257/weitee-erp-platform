package cn.weitee.erp.module.erp.controller.admin.purchase.vo.sourcebatch;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

import static cn.weitee.erp.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "管理后台 - ERP 采购来源批次新增/修改 Request VO")
@Data
public class ErpPurchaseSourceBatchSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "采购订单明细编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "采购订单明细编号不能为空")
    private Long purchaseOrderItemId;

    @Schema(description = "业务日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "业务日期不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate bizDate;

    @Schema(description = "备注", example = "首批采购来源")
    private String remark;
}
