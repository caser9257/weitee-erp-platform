package cn.weitee.erp.module.erp.controller.admin.product.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import cn.weitee.erp.framework.common.pojo.PageParam;

@Schema(description = "管理后台 - 研发物料(Cadence)分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpRdCadencePageReqVO extends PageParam {

    @Schema(description = "物料编号", example = "MAT-001")
    private String materialCode;

    @Schema(description = "物料名称", example = "电阻")
    private String name;

    @Schema(description = "产品分类编号", example = "11161")
    private Long categoryId;

    @Schema(description = "Cadence 数据完整性：true=完整，false=缺失", example = "true")
    private Boolean cadenceComplete;

}
