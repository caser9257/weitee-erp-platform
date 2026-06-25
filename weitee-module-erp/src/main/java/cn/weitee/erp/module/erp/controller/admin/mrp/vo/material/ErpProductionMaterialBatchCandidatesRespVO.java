package cn.weitee.erp.module.erp.controller.admin.mrp.vo.material;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ErpProductionMaterialBatchCandidatesRespVO {

    private Long productionMaterialId;

    private Long materialId;

    private Long warehouseId;

    private BigDecimal requiredQty;

    private BigDecimal issuedQty;

    private BigDecimal returnedQty;

    private BigDecimal remainingQty;

    private List<BatchCandidate> batchCandidates;

    @Data
    public static class BatchCandidate {

        private Long stockBatchId;

        private String batchNo;

        private LocalDateTime inboundTime;

        private LocalDate produceDate;

        private LocalDate expireDate;

        private BigDecimal availableQty;

    }

}
