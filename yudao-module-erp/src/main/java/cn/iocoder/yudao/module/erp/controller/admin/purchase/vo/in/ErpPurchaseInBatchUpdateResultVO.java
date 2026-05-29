package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Schema(description = "管理后台 - ERP 采购入库批量修改 Response VO")
@Data
public class ErpPurchaseInBatchUpdateResultVO {

    @Schema(description = "成功数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer successCount;

    @Schema(description = "失败数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer failureCount;

    @Schema(description = "成功更新的编号列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> updatedIds = Collections.emptyList();

    @Schema(description = "失败明细", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<FailedItemVO> failedItems = Collections.emptyList();

    @Data
    public static class FailedItemVO {

        @Schema(description = "采购入库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long id;

        @Schema(description = "失败原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "当前采购入库单不存在")
        private String message;

    }

}
