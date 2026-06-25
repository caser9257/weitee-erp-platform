package cn.weitee.erp.module.erp.controller.admin.rd.vo.bom;

import cn.weitee.erp.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP 研发 BOM 分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpRdBomPageReqVO extends PageParam {

    @Schema(description = "产品编号")
    private Long productId;

    @Schema(description = "BOM 编码")
    private String bomCode;

    @Schema(description = "状态")
    private Integer status;

}
