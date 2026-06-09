package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 费用类型 VO
 * 支持核心枚举类型和字典扩展类型的混合模式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "费用类型 VO")
public class ExpenseTypeVO {

    @Schema(description = "类型值", example = "10")
    private Integer value;

    @Schema(description = "类型名称", example = "研发费用")
    private String label;

    @Schema(description = "是否为核心类型（系统预设）")
    private boolean core;

    @Schema(description = "是否需要项目")
    private boolean projectRequired;

    @Schema(description = "是否需要成本中心")
    private boolean costCenterRequired;

    @Schema(description = "是否需要租赁合同")
    private boolean leaseContractRequired;

    @Schema(description = "是否标记为资产候选")
    private boolean assetCandidateFlag;

    @Schema(description = "费用分类", example = "ADMIN")
    private String category;

    @Schema(description = "是否自动生成凭证")
    private boolean autoGenerateVoucher;

    @Schema(description = "凭证业务类型")
    private Integer voucherBizType;
}
