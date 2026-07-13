package cn.weitee.erp.module.erp.controller.admin.stock.vo.assemble;

import cn.weitee.erp.framework.common.pojo.PageParam;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - ERP 组装拆卸单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ErpStockAssemblePageReqVO extends PageParam {

    private String no;
    private String actionType;
    private Long warehouseId;
    private Long productId;
    private Long detailProductId;

    @InEnum(ErpAuditStatus.class)
    private Integer status;
}
