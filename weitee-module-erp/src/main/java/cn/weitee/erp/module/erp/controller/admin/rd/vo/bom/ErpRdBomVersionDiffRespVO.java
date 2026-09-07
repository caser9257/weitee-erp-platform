package cn.weitee.erp.module.erp.controller.admin.rd.vo.bom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - ERP 研发 BOM 版本明细对比结果 VO")
@Data
public class ErpRdBomVersionDiffRespVO implements Serializable {

    @Schema(description = "旧版本 BOM 编号")
    private Long sourceBomId;

    @Schema(description = "旧版本号", example = "V1.0")
    private String sourceVersion;

    @Schema(description = "新版本 BOM 编号")
    private Long targetBomId;

    @Schema(description = "新版本号", example = "V1.1")
    private String targetVersion;

    @Schema(description = "新增明细数")
    private Integer addedCount;

    @Schema(description = "删除明细数")
    private Integer removedCount;

    @Schema(description = "修改明细数")
    private Integer changedCount;

    @Schema(description = "未变化明细数")
    private Integer unchangedCount;

    @Schema(description = "对比条目列表")
    private List<Entry> entries;

    @Schema(description = "单条对比条目")
    @Data
    public static class Entry implements Serializable {

        @Schema(description = "变更类型：ADDED=新增 / REMOVED=删除 / CHANGED=修改 / UNCHANGED=未变", example = "CHANGED")
        private String changeType;

        @Schema(description = "物料编号")
        private Long materialId;

        @Schema(description = "物料名称")
        private String materialName;

        @Schema(description = "物料类型：1=自制/装配体 0=采购件")
        private Integer materialType;

        @Schema(description = "旧版本明细快照（新增时为空）")
        private ItemSnapshot oldItem;

        @Schema(description = "新版本明细快照（删除时为空）")
        private ItemSnapshot newItem;

        @Schema(description = "字段级变化列表（仅 CHANGED 时有值）")
        private List<FieldChange> changes;

    }

    @Schema(description = "明细快照")
    @Data
    public static class ItemSnapshot implements Serializable {

        private BigDecimal usageQty;

        private String referenceDesignator;

        private String position;

        private BigDecimal lossRate;

        private Integer leadTimeDay;

        private String remark;

    }

    @Schema(description = "字段级变化")
    @Data
    public static class FieldChange implements Serializable {

        @Schema(description = "字段名", example = "usageQty")
        private String field;

        @Schema(description = "字段中文名", example = "用量")
        private String label;

        @Schema(description = "旧值")
        private String oldValue;

        @Schema(description = "新值")
        private String newValue;

    }

}
