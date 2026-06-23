package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.arstatement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 应收台账汇总响应 VO
 *
 * @author system
 */
@Schema(description = "ERP - 应收台账汇总响应 VO")
@Data
public class ErpArStatementSummaryRespVO {

    @Schema(description = "客户ID", example = "1024")
    private Long customerId;

    @Schema(description = "客户名称", example = "示例客户")
    private String customerName;

    @Schema(description = "应收总额", example = "100000.00")
    private BigDecimal totalAmount;

    @Schema(description = "已收总额", example = "50000.00")
    private BigDecimal totalReceivedAmount;

    @Schema(description = "剩余应收总额", example = "50000.00")
    private BigDecimal totalRemainAmount;

    @Schema(description = "台账数量", example = "10")
    private Long statementCount;

}
