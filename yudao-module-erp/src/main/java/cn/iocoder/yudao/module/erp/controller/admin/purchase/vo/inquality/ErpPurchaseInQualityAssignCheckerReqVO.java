package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 采购入库 IQC 指派质检人 Request VO")
@Data
public class ErpPurchaseInQualityAssignCheckerReqVO {

    @Schema(description = "质检单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "质检单编号不能为空")
    private Long id;

    @Schema(description = "指派质检人编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "指派质检人不能为空")
    private Long assignedCheckerUserId;

}
