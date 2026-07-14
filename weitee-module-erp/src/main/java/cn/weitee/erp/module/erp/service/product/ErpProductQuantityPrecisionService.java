package cn.weitee.erp.module.erp.service.product;

import java.math.BigDecimal;

/**
 * 产品数量精度校验服务。
 */
public interface ErpProductQuantityPrecisionService {

    /**
     * 校验数量是否符合产品单位配置的精度。
     *
     * @param productId 产品编号
     * @param quantity 数量，可为负数
     */
    void validateProductQuantity(Long productId, BigDecimal quantity);

}
