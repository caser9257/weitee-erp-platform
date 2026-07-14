package cn.weitee.erp.module.erp.service.product;

import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductUnitDO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_QUANTITY_PRECISION_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_UNIT_NOT_EXISTS;

/**
 * 产品数量精度校验服务实现。
 */
@Service
@Validated
public class ErpProductQuantityPrecisionServiceImpl implements ErpProductQuantityPrecisionService {

    private static final int DEFAULT_QUANTITY_PRECISION = 3;

    @Resource
    private ErpProductService productService;
    @Resource
    private ErpProductUnitService productUnitService;

    @Override
    public void validateProductQuantity(Long productId, BigDecimal quantity) {
        if (quantity == null) {
            return;
        }
        ErpProductDO product = productService.getProduct(productId);
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        ErpProductUnitDO unit = productUnitService.getProductUnit(product.getUnitId());
        if (unit == null) {
            throw exception(PRODUCT_UNIT_NOT_EXISTS);
        }
        int quantityPrecision = unit.getQuantityPrecision() != null
                ? unit.getQuantityPrecision() : DEFAULT_QUANTITY_PRECISION;
        if (quantity.stripTrailingZeros().scale() > quantityPrecision) {
            throw exception(PRODUCT_QUANTITY_PRECISION_INVALID, product.getName(), quantityPrecision);
        }
    }

}
