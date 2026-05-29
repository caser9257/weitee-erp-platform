package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.outsource;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP 委外订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpOutsourceOrderPageReqVO extends PageParam {

    @Schema(description = "委外订单号", example = "WWDD202604280001")
    private String no;

    @Schema(description = "委外类型", example = "10")
    private Integer orderType;

    @Schema(description = "供应商编号", example = "1")
    private Long supplierId;

    @Schema(description = "成品编号", example = "1")
    private Long productId;

    @Schema(description = "状态", example = "20")
    private Integer status;

}
