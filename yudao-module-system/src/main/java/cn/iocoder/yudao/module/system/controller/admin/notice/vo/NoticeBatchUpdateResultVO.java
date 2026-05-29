package cn.iocoder.yudao.module.system.controller.admin.notice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Schema(description = "管理后台 - 通知公告批量修改 Response VO")
@Data
public class NoticeBatchUpdateResultVO {

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

        @Schema(description = "通知公告编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long id;

        @Schema(description = "失败原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "当前通知公告不存在")
        private String message;

    }

}
