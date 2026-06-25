package cn.weitee.erp.module.erp.controller.admin.stock.vo.batch;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 历史出库批次重建 Request VO")
@Data
public class ErpStockBatchRebuildOutboundReqVO {

    @Schema(description = "出库业务类型：20 其它出库，50 销售出库", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    @NotNull(message = "业务类型不能为空")
    private Integer bizType;

    @Schema(description = "业务单据编号；为空时按限制条数扫描", example = "100")
    private Long bizId;

    @Schema(description = "扫描单据上限", example = "100")
    @Min(value = 1, message = "扫描单据上限最小为 1")
    @Max(value = 200, message = "扫描单据上限最大为 200")
    private Integer limit = 100;

    @Schema(description = "执行重建确认；执行接口必须传 true", example = "true")
    private Boolean confirm;

}
