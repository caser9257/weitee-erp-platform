package cn.weitee.erp.module.erp.controller.admin.rd.vo.bom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - ERP 研发 BOM 反向追溯（Where-Used）Response VO")
@Data
public class ErpRdBomWhereUsedRespVO {

    @Schema(description = "直接引用该物料的研发 BOM 编号")
    private Long bomId;

    @Schema(description = "研发 BOM 编码")
    private String bomCode;

    @Schema(description = "成品编号（该 BOM 所属成品）")
    private Long productId;

    @Schema(description = "成品名称")
    private String productName;

    @Schema(description = "版本")
    private String version;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "层级（1=直接父，2=祖父...）")
    private Integer level;

    @Schema(description = "上级（递归向上）的 Where-Used 链")
    private List<ErpRdBomWhereUsedRespVO> parents;

}
