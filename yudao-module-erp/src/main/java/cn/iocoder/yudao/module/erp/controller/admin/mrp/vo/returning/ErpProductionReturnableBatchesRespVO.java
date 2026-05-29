package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.returning;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ErpProductionReturnableBatchesRespVO {

    private Long productionMaterialId;

    private Long materialId;

    private Long warehouseId;

    private List<ReturnableBatch> returnableBatches;

    @Data
    public static class ReturnableBatch {

        private Long issueBatchId;

        private Long stockBatchId;

        private String batchNo;

        private Long warehouseId;

        private BigDecimal issuedQty;

        private BigDecimal returnedQty;

        private BigDecimal returnableQty;

    }

}
