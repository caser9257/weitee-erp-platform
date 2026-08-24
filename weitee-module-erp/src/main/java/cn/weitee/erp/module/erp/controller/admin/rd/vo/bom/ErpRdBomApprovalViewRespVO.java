package cn.weitee.erp.module.erp.controller.admin.rd.vo.bom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Schema(description = "管理后台 - ERP 研发 BOM 审批视图 VO（详细内容 + 版本差异 + 变更记录）")
@Data
public class ErpRdBomApprovalViewRespVO implements Serializable {

    @Schema(description = "BOM 详细内容（表头 + 明细 + 替代料）")
    private ErpRdBomRespVO bom;

    @Schema(description = "是否首次提交（无对比基准版本）")
    private Boolean firstSubmit;

    @Schema(description = "对比基准版本 BOM 编号（firstSubmit=true 时为空）", example = "1024")
    private Long baselineBomId;

    @Schema(description = "对比基准版本号", example = "V1.0")
    private String baselineVersion;

    @Schema(description = "与基准版本的明细差异（firstSubmit=true 时为空）")
    private ErpRdBomVersionDiffRespVO diff;

    @Schema(description = "变更记录（最近在前，最多 10 条）")
    private List<ErpRdBomChangeLogRespVO> changeLogs;

}
