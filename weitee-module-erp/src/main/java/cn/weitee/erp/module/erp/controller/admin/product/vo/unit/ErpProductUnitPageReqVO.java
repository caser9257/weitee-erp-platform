package cn.weitee.erp.module.erp.controller.admin.product.vo.unit;

import cn.weitee.erp.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP 产品单位分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpProductUnitPageReqVO extends PageParam {

    @Schema(description = "单位名字", example = "芋艿")
    private String name;

    @Schema(description = "单位状态", example = "1")
    private Integer status;

    @Schema(description = "单位类型，0 基本单位 1 辅助单位", example = "0")
    private Integer unitType;

    @Schema(description = "基本单位编号", example = "1")
    private Long baseUnitId;

}