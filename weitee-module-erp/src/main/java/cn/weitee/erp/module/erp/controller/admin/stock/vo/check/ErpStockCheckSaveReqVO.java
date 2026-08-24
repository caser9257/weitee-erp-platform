package cn.weitee.erp.module.erp.controller.admin.stock.vo.check;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 库存盘点单新增/修改 Request VO")
@Data
public class ErpStockCheckSaveReqVO {

    @Schema(description = "盘点编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11756")
    private Long id;

    @Schema(description = "归属部门ID（数据权限），为空时默认取创建人所属部门", example = "1024")
    private Long deptId;

    @Schema(description = "盘点时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "盘点时间不能为空")
    private LocalDateTime checkTime;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "附件 URL", example = "https://www.iocoder.cn/1.doc")
    private String fileUrl;

    @Schema(description = "盘点项列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "盘点项列表不能为空")
    @Valid
    private List<Item> items;

    @Data
    public static class Item {

        @Schema(description = "盘点项编号", example = "11756")
        private Long id;

        @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        @NotNull(message = "仓库编号不能为空")
        private Long warehouseId;

        @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        @NotNull(message = "产品编号不能为空")
        private Long productId;

        @Schema(description = "产品单价", example = "100.00")
        private BigDecimal productPrice;

        @Schema(description = "账面数量（当前库存）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        @NotNull(message = "账面数量不能为空")
        private BigDecimal stockCount;

        @Schema(description = "实际数量（实际库存）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        @NotNull(message = "实际数量不能为空")
        private BigDecimal actualCount;

        @Schema(description = "盈亏数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        @NotNull(message = "盈亏数量不能为空")
        private BigDecimal count;

        @Schema(description = "备注", example = "随便")
        private String remark;

    }

}
