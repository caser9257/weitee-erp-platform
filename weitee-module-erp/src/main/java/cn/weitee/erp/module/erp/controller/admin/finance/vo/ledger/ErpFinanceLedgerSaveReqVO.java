package cn.weitee.erp.module.erp.controller.admin.finance.vo.ledger;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 财务账簿新增/修改 Request VO")
@Data
public class ErpFinanceLedgerSaveReqVO {

    @Schema(description = "账簿编号", example = "1")
    private Long id;

    @Schema(description = "账簿编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "BOOK-STD")
    @NotEmpty(message = "账簿编码不能为空")
    private String no;

    @Schema(description = "账簿名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "标准账簿")
    @NotEmpty(message = "账簿名称不能为空")
    private String name;

    @Schema(description = "启用状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "启用状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "是否默认账簿", example = "true")
    private Boolean defaultStatus;

    @Schema(description = "备注", example = "采购财务主账簿")
    private String remark;
}
