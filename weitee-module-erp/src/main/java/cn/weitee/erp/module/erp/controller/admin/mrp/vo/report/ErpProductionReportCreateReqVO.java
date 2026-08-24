package cn.weitee.erp.module.erp.controller.admin.mrp.vo.report;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ErpProductionReportCreateReqVO {

    @NotNull(message = "生产工单不能为空")
    private Long productionOrderId;

    @NotNull(message = "报工类型不能为空")
    private Integer reportType;

    private String batchNo;

    private String remark;

    @NotEmpty(message = "报工明细不能为空")
    @Valid
    private List<Item> items;

    @Data
    public static class Item {

        @NotNull(message = "工单工序不能为空")
        private Long productionOrderStepId;

        private Long deviceId;

        private Long workerUserId;

        @NotNull(message = "报工数量不能为空")
        @DecimalMin(value = "0.000001", message = "报工数量必须大于 0")
        private BigDecimal reportedQty;

        @NotNull(message = "合格数量不能为空")
        @DecimalMin(value = "0", message = "合格数量不能小于 0")
        private BigDecimal qualifiedQty;

        @NotNull(message = "报废数量不能为空")
        @DecimalMin(value = "0", message = "报废数量不能小于 0")
        private BigDecimal scrapQty;

        private BigDecimal workHour;

        private String batchNo;

        private String remark;
    }

}
