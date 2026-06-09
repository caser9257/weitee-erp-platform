package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.invoice;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ERP 销项发票保存 VO
 *
 * @author system
 */
@Schema(description = "ERP 销项发票保存 VO")
@Data
public class ErpInvoiceSaveReqVO {

    @Schema(description = "发票编号", example = "1")
    private Long id;

    @Schema(description = "客户编号", example = "100")
    @NotNull(message = "客户编号不能为空")
    private Long customerId;

    @Schema(description = "订单编号", example = "200")
    @NotNull(message = "订单编号不能为空")
    private Long orderId;

    @Schema(description = "发票类型", example = "NORMAL")
    private String invoiceType;

    @Schema(description = "发票抬头")
    private String invoiceTitle;

    @Schema(description = "纳税人识别号")
    private String taxpayerNo;

    @Schema(description = "开票时间")
    private LocalDateTime invoiceTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "发票明细")
    private List<ErpInvoiceItemSaveReqVO> items;

    /**
     * 发票明细保存 VO
     */
    @Data
    public static class ErpInvoiceItemSaveReqVO {

        @Schema(description = "产品编号")
        @NotNull(message = "产品编号不能为空")
        private Long productId;

        @Schema(description = "产品名称")
        private String productName;

        @Schema(description = "产品规格")
        private String productSpec;

        @Schema(description = "单位")
        private String unit;

        @Schema(description = "数量")
        @NotNull(message = "数量不能为空")
        private BigDecimal count;

        @Schema(description = "单价（不含税）")
        @NotNull(message = "单价不能为空")
        private BigDecimal price;

        @Schema(description = "税率")
        private BigDecimal taxRate;

        @Schema(description = "备注")
        private String remark;

    }

}
