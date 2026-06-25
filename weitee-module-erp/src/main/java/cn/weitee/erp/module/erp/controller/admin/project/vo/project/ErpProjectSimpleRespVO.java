package cn.weitee.erp.module.erp.controller.admin.project.vo.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - ERP 项目精简 Response VO")
@Data
public class ErpProjectSimpleRespVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "项目编号", example = "XM202604020001")
    private String no;

    @Schema(description = "项目名称", example = "MES 阶段一")
    private String name;

}
