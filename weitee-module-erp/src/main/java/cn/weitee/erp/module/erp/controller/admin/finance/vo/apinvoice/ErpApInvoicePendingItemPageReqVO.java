package cn.weitee.erp.module.erp.controller.admin.finance.vo.apinvoice;

import cn.weitee.erp.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP 采购发票待匹配明细分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpApInvoicePendingItemPageReqVO extends PageParam {

    @Schema(description = "采购发票编号", example = "1")
    private Long invoiceId;

    @Schema(description = "供应商编号", example = "201")
    private Long supplierId;

    @Schema(description = "采购订单号", example = "PO-001")
    private String sourceOrderNo;

    @Schema(description = "采购入库单号", example = "PI-001")
    private String purchaseInNo;

}
