package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.outsource;

import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ErpOutsourceIssueCreateReqVO {

    @NotNull(message = "orderId cannot be empty")
    private Long orderId;

    private Integer issueType;

    private String remark;

    @Valid
    @NotEmpty(message = "items cannot be empty")
    private List<Item> items;

    @Data
    public static class Item {
        @NotNull(message = "materialId cannot be empty")
        private Long materialId;
        @NotNull(message = "warehouseId cannot be empty")
        private Long warehouseId;
        @NotNull(message = "issueQty cannot be empty")
        @DecimalMin(value = "0.000001", message = "issueQty must be greater than 0")
        private BigDecimal issueQty;
        private String remark;
        @Valid
        @NotEmpty(message = "batches cannot be empty")
        private List<Batch> batches;
    }

    @Data
    public static class Batch {
        @NotNull(message = "stockBatchId cannot be empty")
        private Long stockBatchId;
        @NotBlank(message = "batchNo cannot be empty")
        private String batchNo;
        @NotNull(message = "issueQty cannot be empty")
        @DecimalMin(value = "0.000001", message = "issueQty must be greater than 0")
        private BigDecimal issueQty;
    }

}
