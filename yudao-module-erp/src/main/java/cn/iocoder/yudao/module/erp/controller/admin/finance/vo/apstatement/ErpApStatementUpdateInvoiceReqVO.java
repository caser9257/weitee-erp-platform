package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.erp.enums.ErpApInvoiceStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 应付台账收票登记 Request VO")
@Data
public class ErpApStatementUpdateInvoiceReqVO {

    @Schema(description = "台账编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "台账编号不能为空")
    private Long id;

    @Schema(description = "发票状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "发票状态不能为空")
    @InEnum(value = ErpApInvoiceStatusEnum.class, message = "发票状态必须是 {value}")
    private Integer invoiceStatus;

    @Schema(description = "发票号", example = "FP-20260424001")
    private String invoiceNo;

    @Schema(description = "发票金额", example = "1200.00")
    @DecimalMin(value = "0", message = "发票金额不能小于 0")
    private BigDecimal invoiceAmount;

    @Schema(description = "收票备注", example = "补录 4 月发票")
    private String remark;

}
