package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "管理后台 - ERP 采购入库批量修改 Request VO")
@Data
public class ErpPurchaseInBatchUpdateReqVO {

    @Schema(description = "采购入库编号列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1,2]")
    @NotEmpty(message = "采购入库编号不能为空")
    private List<Long> ids;

    @Schema(description = "修改字段标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "remark")
    @NotBlank(message = "修改字段标识不能为空")
    private String fieldKey;

    @Schema(description = "修改模式，参见 BatchEditModeEnum 枚举", requiredMode = Schema.RequiredMode.REQUIRED, example = "overwrite")
    @NotBlank(message = "修改模式不能为空")
    private String mode;

    @Schema(description = "修改值", requiredMode = Schema.RequiredMode.REQUIRED, example = "补充备注")
    @NotBlank(message = "修改值不能为空")
    private String value;

}
