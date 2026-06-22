package cn.iocoder.yudao.module.erp.controller.admin.sale.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 发货放行分页请求 VO
 *
 * @author system
 */
@Schema(description = "ERP - 发货放行分页请求 VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ShipmentReleasePageReqVO extends PageParam {

    @Schema(description = "销售订单号", example = "SO20240001")
    private String orderNo;

    @Schema(description = "项目编号", example = "PRJ20240001")
    private String projectNo;

    @Schema(description = "合同编号", example = "CON20240001")
    private String contractNo;

    @Schema(description = "客户编号", example = "1024")
    private Long customerId;

    @Schema(description = "销售员编号", example = "1024")
    private Long saleUserId;

    @Schema(description = "发货放行状态", example = "RELEASED")
    private String releaseStatus;

    @Schema(description = "交期开始日期")
    private String deliveryDateStart;

    @Schema(description = "交期结束日期")
    private String deliveryDateEnd;

}
