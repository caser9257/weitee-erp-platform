package cn.weitee.erp.module.erp.controller.admin.stock.vo.warehouse;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.PageParam;
import cn.weitee.erp.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP 仓库分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpWarehousePageReqVO extends PageParam {

    @Schema(description = "仓库名称", example = "李四")
    private String name;

    @Schema(description = "仓库分类编号", example = "1")
    private Long categoryId;

    @Schema(description = "开启状态", example = "1")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

}
