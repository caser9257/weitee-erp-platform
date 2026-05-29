package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.dualledger;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 双账套账簿映射新增/修改 Request VO")
@Data
public class ErpFinanceDualLedgerConfigSaveReqVO {

    @Schema(description = "映射编号", example = "1")
    private Long id;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "11")
    @NotNull(message = "业务类型不能为空")
    @InEnum(ErpBizTypeEnum.class)
    private Integer bizType;

    @Schema(description = "对外账账簿编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "99601")
    @NotNull(message = "对外账账簿不能为空")
    private Long externalLedgerId;

    @Schema(description = "内部账账簿编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "99602")
    @NotNull(message = "内部账账簿不能为空")
    private Long internalLedgerId;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "备注", example = "采购入库双账套映射")
    private String remark;
}
