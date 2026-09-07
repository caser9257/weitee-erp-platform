package cn.weitee.erp.module.erp.controller.admin.rd.vo.bom;

import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomIntegrityIssueRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Schema(description = "管理后台 - ERP 研发 BOM 导入结果 VO")
@Data
public class ErpRdBomImportResultVO implements Serializable {

    @Schema(description = "导入后创建的研发 BOM 编号（全部行解析失败时为空）")
    private Long bomId;

    @Schema(description = "数据总行数（不含表头）", example = "10")
    private Integer totalCount;

    @Schema(description = "成功导入的明细行数", example = "8")
    private Integer successCount;

    @Schema(description = "解析失败的行数", example = "2")
    private Integer failCount;

    @Schema(description = "解析失败明细")
    private List<FailDetail> failDetails;

    @Schema(description = "重复产品编码提醒清单；仅提醒，不阻断导入")
    private List<ErpRdBomPrecheckResultVO.DuplicateMaterialCode> duplicateMaterialCodes;

    @Schema(description = "已存在的同身份研发 BOM；存在时导入被阻断")
    private ErpRdBomPrecheckResultVO.DuplicateBom duplicateBom;

    @Schema(description = "较该成品最新版 BOM 缺失的物料对比；null 表示未对比（首次导入/无法确定成品）")
    private ErpRdBomBaselineDiffVO baselineDiff;

    @Schema(description = "导入后自动完整性校验（位号/用量/悬浮件）问题清单，空表示通过")
    private List<ErpRdBomIntegrityIssueRespVO> validationIssues;

    @Schema(description = "研发 BOM 导入失败明细")
    @Data
    public static class FailDetail implements Serializable {

        @Schema(description = "Excel 行号（从 1 开始，含表头）", example = "5")
        private Integer rowNumber;

        @Schema(description = "物料编号", example = "MAT-1001")
        private String materialCode;

        @Schema(description = "失败原因")
        private String reason;

        @Schema(description = "失败分类：MISSING_MATERIAL=物料未建档 / MATERIAL_NOT_APPROVED=未审核通过 / MATERIAL_DISABLED=已停用 / CADENCE_DATA_INCOMPLETE=Cadence 数据不完整 / FORMAT_ERROR=格式错误", example = "FORMAT_ERROR")
        private String issueType;

    }

}
