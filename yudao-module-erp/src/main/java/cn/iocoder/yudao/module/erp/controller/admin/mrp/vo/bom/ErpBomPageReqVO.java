package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.bom;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP BOM 分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpBomPageReqVO extends PageParam {

    @Schema(description = "产品编号")
    private Long productId;

    @Schema(description = "BOM 编号")
    private String bomCode;

    @Schema(description = "状态")
    private Integer status;

}
