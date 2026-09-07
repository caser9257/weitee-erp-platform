package cn.weitee.erp.module.erp.service.product;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * ERP 单据明细录入单位换算 Service 接口
 *
 * 统一入口：单据明细按"录入单位"（产品基本单位或其辅助单位）录入数量，
 * 由该服务校验单位族归属与数量精度，并换算为产品基本单位数量用于记账。
 * count 语义保持为基本单位记账数量，下游库存、统计、财务链路不受影响。
 */
public interface ErpProductUnitConversionService {

    /**
     * 校验并换算单据明细的录入单位数量
     *
     * @param productId   产品编号
     * @param inputUnitId 录入单位编号，null 表示按产品基本单位录入
     * @param inputCount  录入数量（录入单位口径），可为负数（如盘亏）
     * @return 换算结果
     */
    ConversionResult convert(Long productId, Long inputUnitId, BigDecimal inputCount);

    /**
     * 批量校验并换算单据明细的录入单位数量，内部批量查询产品与单位，避免 N+1
     *
     * @param requests 换算请求，顺序与返回列表一致
     * @return 换算结果列表
     */
    List<ConversionResult> convertBatch(List<ConversionRequest> requests);

    /**
     * 换算请求
     */
    @Data
    @AllArgsConstructor
    class ConversionRequest {

        /**
         * 产品编号
         */
        private Long productId;
        /**
         * 录入单位编号，null 表示按产品基本单位录入
         */
        private Long inputUnitId;
        /**
         * 录入数量（录入单位口径）
         */
        private BigDecimal inputCount;

    }

    /**
     * 换算结果
     */
    @Data
    @AllArgsConstructor
    class ConversionResult {

        /**
         * 实际录入单位编号
         */
        private Long inputUnitId;
        /**
         * 录入数量（录入单位口径）
         */
        private BigDecimal inputCount;
        /**
         * 换算率快照：1 录入单位 = conversionRate 基本单位；基本单位为 null
         */
        private BigDecimal conversionRate;
        /**
         * 基本单位数量（记账数量）
         */
        private BigDecimal baseCount;

    }

}
