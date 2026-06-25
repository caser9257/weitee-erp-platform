package cn.weitee.erp.module.erp.controller.admin.stock.vo.batch;

import cn.weitee.erp.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "ERP stock batch page request")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpStockBatchPageReqVO extends PageParam {

    @Schema(description = "Product id", example = "1024")
    private Long productId;

    @Schema(description = "Warehouse id", example = "1")
    private Long warehouseId;

    @Schema(description = "Batch no", example = "B20260424001")
    private String batchNo;

}
