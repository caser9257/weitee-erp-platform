package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.dualledger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 双账套差异项口径配置 Response VO")
@Data
public class ErpFinanceDualLedgerDiffConfigRespVO {

    @Schema(description = "配置编号", example = "1")
    private Long id;

    @Schema(description = "业务类型", example = "11")
    private Integer bizType;

    @Schema(description = "业务类型名称", example = "采购入库")
    private String bizTypeName;

    @Schema(description = "差异项类型", example = "20")
    private Integer diffItemType;

    @Schema(description = "差异项类型名称", example = "人工成本")
    private String diffItemTypeName;

    @Schema(description = "对外账来源类型", example = "10")
    private Integer externalSourceType;

    @Schema(description = "对外账来源类型名称", example = "生产成本项目")
    private String externalSourceTypeName;

    @Schema(description = "对外账来源值", example = "20")
    private Integer externalSourceValue;

    @Schema(description = "对外账来源值名称", example = "人工成本")
    private String externalSourceValueName;

    @Schema(description = "内部账来源类型", example = "20")
    private Integer internalSourceType;

    @Schema(description = "内部账来源类型名称", example = "固定资产折旧")
    private String internalSourceTypeName;

    @Schema(description = "内部账来源值", example = "60")
    private Integer internalSourceValue;

    @Schema(description = "内部账来源值名称", example = "制造费用")
    private String internalSourceValueName;

    @Schema(description = "计算类型", example = "1")
    private Integer calculationType;

    @Schema(description = "计算类型名称", example = "按比例分摊")
    private String calculationTypeName;

    @Schema(description = "比例系数", example = "0.85")
    private BigDecimal ratio;

    @Schema(description = "固定差额", example = "20.00")
    private BigDecimal fixedAmount;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "人工成本对外账按直接人工，内部账按制造费用吸收")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
