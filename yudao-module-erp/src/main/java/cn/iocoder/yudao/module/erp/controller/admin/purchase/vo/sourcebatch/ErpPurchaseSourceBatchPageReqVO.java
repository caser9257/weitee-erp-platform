package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.sourcebatch;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "管理后台 - ERP 采购来源批次分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpPurchaseSourceBatchPageReqVO extends PageParam {

    @Schema(description = "来源批次号", example = "CGLY20260427000001")
    private String batchNo;

    @Schema(description = "产品编号", example = "1")
    private Long productId;

    @Schema(description = "采购订单编号", example = "1")
    private Long purchaseOrderId;

    @Schema(description = "采购订单明细编号", example = "1")
    private Long purchaseOrderItemId;

    @Schema(description = "供应商编号", example = "1")
    private Long supplierId;

    @Schema(description = "状态", example = "20")
    private Integer status;

    @Schema(description = "业务日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate[] bizDate;
}
