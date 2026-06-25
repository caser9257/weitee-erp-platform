package cn.weitee.erp.module.erp.controller.admin.sale.vo;

import cn.weitee.erp.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 市场执行台账分页查询 VO
 *
 * @author system
 */
@Schema(description = "市场执行台账分页查询 VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MarketLedgerPageReqVO extends PageParam {

    @Schema(description = "项目编号", example = "PRJ-2026-001")
    private String projectNo;

    @Schema(description = "合同编号", example = "CRM-CT-2026-012")
    private String contractNo;

    @Schema(description = "客户名称", example = "某某科技有限公司")
    private String customerName;

    @Schema(description = "销售员编号", example = "100")
    private Long saleUserId;

    @Schema(description = "生命周期阶段", example = "CONTRACT")
    private String lifecycleStage;

    @Schema(description = "放行状态", example = "RELEASED")
    private String releaseStatus;

    @Schema(description = "开票状态", example = "NOT_INVOICED")
    private String invoiceStatus;

    @Schema(description = "验收状态", example = "PENDING")
    private String acceptanceStatus;

    @Schema(description = "订单月份（格式：yyyy-MM）", example = "2026-06")
    private String orderMonth;

    @Schema(description = "交期开始日期", example = "2026-01-01")
    private String deliveryDateStart;

    @Schema(description = "交期结束日期", example = "2026-12-31")
    private String deliveryDateEnd;

}
