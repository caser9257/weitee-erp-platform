package cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger;

import cn.weitee.erp.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - ERP 双账套差异项口径配置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpFinanceDualLedgerDiffConfigPageReqVO extends PageParam {

    @Schema(description = "业务类型", example = "11")
    private Integer bizType;

    @Schema(description = "差异项类型", example = "20")
    private Integer diffItemType;

    @Schema(description = "对外账来源类型", example = "10")
    private Integer externalSourceType;

    @Schema(description = "内部账来源类型", example = "20")
    private Integer internalSourceType;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "人工成本外账按人工、内账按制造费用口径")
    private String remark;

}
