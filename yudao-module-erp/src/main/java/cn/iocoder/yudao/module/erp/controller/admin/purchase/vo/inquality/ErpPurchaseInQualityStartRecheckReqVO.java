package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 采购入库 IQC 发起复检 Request VO")
@Data
public class ErpPurchaseInQualityStartRecheckReqVO {

    @Schema(description = "质检单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "质检单编号不能为空")
    private Long id;

    @Schema(description = "复检原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "初检发现异常，需要复核")
    @NotBlank(message = "复检原因不能为空")
    private String recheckReason;

}
