package cn.weitee.erp.module.erp.controller.admin.purchase.vo.in;

import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ErpPurchaseInStockExecuteCreateReqVO {

    @NotNull(message = "purchaseInId cannot be empty")
    private Long purchaseInId;

    private String remark;

    @Valid
    @NotEmpty(message = "items cannot be empty")
    private List<Item> items;

    @Data
    public static class Item {

        @NotNull(message = "purchaseInItemId cannot be empty")
        private Long purchaseInItemId;

        @NotNull(message = "count cannot be empty")
        @DecimalMin(value = "0.000001", message = "count must be greater than 0")
        private BigDecimal count;

        private String remark;

        @Valid
        private List<Batch> batches;

    }

    @Data
    public static class Batch {

        @NotBlank(message = "batchNo cannot be empty")
        private String batchNo;

        @NotNull(message = "count cannot be empty")
        @DecimalMin(value = "0.000001", message = "count must be greater than 0")
        private BigDecimal count;

        @NotNull(message = "inboundTime cannot be empty")
        private LocalDateTime inboundTime;

        private LocalDate produceDate;

        private LocalDate expireDate;

        private String remark;

    }

}
