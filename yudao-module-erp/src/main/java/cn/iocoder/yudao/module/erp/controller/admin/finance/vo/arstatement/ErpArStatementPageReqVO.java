package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.arstatement;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

/**
 * 应收台账分页请求 VO
 *
 * @author system
 */
@Schema(description = "ERP - 应收台账分页请求 VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpArStatementPageReqVO extends PageParam {

    @Schema(description = "台账编号", example = "AR20240001")
    private String statementNo;

    @Schema(description = "业务类型：21=销售出库 22=销售退货", example = "21")
    private Integer bizType;

    @Schema(description = "业务单据号", example = "SO20240001")
    private String bizNo;

    @Schema(description = "客户ID", example = "1024")
    private Long customerId;

    @Schema(description = "来源销售订单ID", example = "1024")
    private Long sourceOrderId;

    @Schema(description = "结算账户ID", example = "1024")
    private Long accountId;

    @Schema(description = "币种", example = "CNY")
    private String currencyCode;

    @Schema(description = "开票状态：0=未开票 1=部分开票 2=已开票", example = "0")
    private Integer invoiceStatus;

    @Schema(description = "状态：0=待收 1=部分收 2=已结清 3=已关闭", example = "0")
    private Integer status;

    @Schema(description = "业务日期范围")
    private LocalDate[] bizDate;

    @Schema(description = "到期日期范围")
    private LocalDate[] dueDate;

}
