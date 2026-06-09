package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.invoice;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * ERP 销项发票分页查询 VO
 *
 * @author system
 */
@Schema(description = "ERP 销项发票分页查询 VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpInvoicePageReqVO extends PageParam {

    @Schema(description = "发票号", example = "INV-2026-001")
    private String no;

    @Schema(description = "客户编号", example = "100")
    private Long customerId;

    @Schema(description = "订单编号", example = "200")
    private Long orderId;

    @Schema(description = "发票状态", example = "ISSUED")
    private String status;

}
