package cn.weitee.erp.module.erp.controller.admin.rd.vo.bom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Schema(description = "管理后台 - ERP 研发 BOM 导入预检查结果 VO（dry-run，不落库）")
@Data
public class ErpRdBomPrecheckResultVO implements Serializable {

    @Schema(description = "数据总行数（不含表头）", example = "10")
    private Integer totalCount;

    @Schema(description = "可直接导入的行数", example = "7")
    private Integer readyCount;

    @Schema(description = "被物料档案阻断的行数（未建档 + 未审核 + 已停用合计）", example = "2")
    private Integer blockedCount;

    @Schema(description = "格式类错误行数（导入时将被跳过，不阻断）", example = "1")
    private Integer issueCount;

    @Schema(description = "是否可直接导入：待建档与待催审清单均为空时为 true")
    private Boolean readyToImport;

    @Schema(description = "智能识别的表头信息")
    private DetectedHeader detectedHeader;

    @Schema(description = "较该成品最新版 BOM 缺失的物料对比；null 表示未对比（首次导入/无法确定成品）")
    private ErpRdBomBaselineDiffVO baselineDiff;

    @Schema(description = "待建档物料清单")
    private List<MissingMaterial> missingMaterials;

    @Schema(description = "已建档但当前状态不可用的物料清单（未审核通过/已停用）")
    private List<UnapprovedMaterial> unapprovedMaterials;

    @Schema(description = "导入行问题清单")
    private List<RowIssue> rowIssues;

    @Schema(description = "重复产品编码提醒清单；仅提醒，不阻断导入")
    private List<DuplicateMaterialCode> duplicateMaterialCodes;

    @Schema(description = "已存在的同身份研发 BOM；存在时阻断导入")
    private DuplicateBom duplicateBom;

    @Schema(description = "智能识别的表头信息")
    @Data
    public static class DetectedHeader implements Serializable {

        @Schema(description = "识别出的 BOM 编码", example = "PCBA-V1.2")
        private String bomCode;

        @Schema(description = "识别出的版本", example = "V1.2")
        private String version;

        @Schema(description = "识别出的产品名称", example = "控制板组件")
        private String productName;

        @Schema(description = "匹配到的成品编号", example = "1024")
        private Long productId;

        @Schema(description = "顶层物料是否缺失（BOM 编码在系统中无对应档案且未显式传入有效成品）")
        private Boolean topLevelMissing;

    }

    @Schema(description = "待建档物料")
    @Data
    public static class MissingMaterial implements Serializable {

        @Schema(description = "Excel 中的物料编号", example = "MAT-2001")
        private String materialCode;

        @Schema(description = "Excel 中可提取的物料名称（可能为空）", example = "钽电容 10uF")
        private String materialName;

        @Schema(description = "出现的 Excel 行号列表（从 1 开始，含表头）")
        private List<Integer> rowNumbers;

        @Schema(description = "是否为顶层物料（BOM 头编码缺档）")
        private Boolean topLevel;

    }

    @Schema(description = "状态不可用物料")
    @Data
    public static class UnapprovedMaterial implements Serializable {

        @Schema(description = "物料编号", example = "MAT-1001")
        private String materialCode;

        @Schema(description = "物料档案编号", example = "2048")
        private Long materialId;

        @Schema(description = "物料名称", example = "电阻 10K")
        private String productName;

        @Schema(description = "审核状态", example = "10")
        private Integer auditStatus;

        @Schema(description = "启用状态", example = "0")
        private Integer status;

        @Schema(description = "是否已停用（true 表示需先启用；false 表示需先完成审核）")
        private Boolean disabled;

        @Schema(description = "出现的 Excel 行号列表（从 1 开始，含表头）")
        private List<Integer> rowNumbers;

    }

    @Schema(description = "格式类问题")
    @Data
    public static class RowIssue implements Serializable {

        @Schema(description = "Excel 行号（从 1 开始，含表头）", example = "5")
        private Integer rowNumber;

        @Schema(description = "物料编号", example = "MAT-1001")
        private String materialCode;

        @Schema(description = "失败原因")
        private String reason;

        @Schema(description = "问题分类：FORMAT_ERROR=格式错误 / CADENCE_DATA_INCOMPLETE=Cadence 数据不完整")
        private String issueType;

    }

    @Schema(description = "重复产品编码提醒")
    @Data
    public static class DuplicateMaterialCode implements Serializable {

        @Schema(description = "产品编码", example = "MAT-1001")
        private String materialCode;

        @Schema(description = "出现的 Excel 行号列表（从 1 开始，含表头）")
        private List<Integer> rowNumbers;

    }

    @Schema(description = "重复的研发 BOM 主身份")
    @Data
    public static class DuplicateBom implements Serializable {

        @Schema(description = "已存在的研发 BOM 编号")
        private Long id;
        @Schema(description = "BOM 编码")
        private String bomCode;
        @Schema(description = "版本；为空表示草稿")
        private String version;
        @Schema(description = "当前状态")
        private Integer status;
    }

}
