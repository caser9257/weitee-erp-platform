package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apinvoice;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 采购发票分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpApInvoicePageReqVO extends PageParam {

    @Schema(description = "发票号", example = "FP-20260428-001")
    private String invoiceNo;

    @Schema(description = "供应商编号", example = "201")
    private Long supplierId;

    @Schema(description = "发票类型", example = "10")
    private Integer invoiceType;

    @Schema(description = "匹配状态", example = "20")
    private Integer matchStatus;

    @Schema(description = "发票日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] invoiceDate;

    @Schema(description = "备注", example = "月末集中开票")
    private String remark;

}
