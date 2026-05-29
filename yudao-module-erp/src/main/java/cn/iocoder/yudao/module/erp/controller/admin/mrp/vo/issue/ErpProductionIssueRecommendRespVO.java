package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.issue;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ErpProductionIssueRecommendRespVO {

    private BigDecimal totalAvailableQty;

    private BigDecimal gapQty;

    private List<BatchAllocation> batchAllocations;

    @Data
    public static class BatchAllocation {

        private Long stockBatchId;

        private String batchNo;

        private LocalDateTime inboundTime;

        private LocalDate produceDate;

        private LocalDate expireDate;

        private BigDecimal availableQty;

        private BigDecimal recommendedQty;

    }

}
