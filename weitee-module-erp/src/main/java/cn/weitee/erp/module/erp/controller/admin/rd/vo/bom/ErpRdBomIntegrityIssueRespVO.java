package cn.weitee.erp.module.erp.controller.admin.rd.vo.bom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "管理后台 - ERP 研发 BOM 完整性校验问题 VO")
@Data
public class ErpRdBomIntegrityIssueRespVO implements Serializable {

    @Schema(description = "问题类型编码", example = "5")
    private Integer issueType;

    @Schema(description = "严重程度", example = "ERROR")
    private String severity;

    @Schema(description = "问题所在行号（从 1 开始）", example = "3")
    private Integer rowIndex;

    @Schema(description = "物料编号", example = "1024")
    private Long materialId;

    @Schema(description = "物料名称", example = "电阻 10K")
    private String materialName;

    @Schema(description = "问题描述")
    private String message;

}
