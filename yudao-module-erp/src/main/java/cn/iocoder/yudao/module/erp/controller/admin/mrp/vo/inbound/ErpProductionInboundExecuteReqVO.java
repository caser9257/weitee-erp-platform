package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.inbound;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ErpProductionInboundExecuteReqVO {

    @NotNull(message = "自制入库单不能为空")
    private Long id;

}
