package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IQC 不良原因精简 Response VO")
@Data
public class ErpQcDefectReasonSimpleRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "原因编码", example = "LOOK")
    private String code;

    @Schema(description = "原因名称", example = "外观不良")
    private String name;

}
