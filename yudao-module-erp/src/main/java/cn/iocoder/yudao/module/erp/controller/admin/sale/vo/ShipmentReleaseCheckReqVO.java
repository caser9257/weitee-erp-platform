package cn.iocoder.yudao.module.erp.controller.admin.sale.vo;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 发货放行校验请求 VO
 *
 * @author system
 */
@Data
public class ShipmentReleaseCheckReqVO {

    /**
     * 销售订单编号
     */
    @NotNull(message = "销售订单编号不能为空")
    private Long orderId;

}
