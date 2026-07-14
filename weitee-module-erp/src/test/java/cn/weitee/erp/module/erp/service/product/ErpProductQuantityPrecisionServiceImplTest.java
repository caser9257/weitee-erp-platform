package cn.weitee.erp.module.erp.service.product;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductUnitDO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErpProductQuantityPrecisionServiceImplTest {

    @Mock
    private ErpProductService productService;
    @Mock
    private ErpProductUnitService productUnitService;
    @InjectMocks
    private ErpProductQuantityPrecisionServiceImpl service;

    @Test
    void validateProductQuantity_shouldAllowWholeNumberForIntegerUnit() {
        mockProductAndUnit(0);

        assertDoesNotThrow(() -> service.validateProductQuantity(1L, new BigDecimal("2.000")));
    }

    @Test
    void validateProductQuantity_shouldAllowConfiguredDecimalPlaces() {
        mockProductAndUnit(3);

        assertDoesNotThrow(() -> service.validateProductQuantity(1L, new BigDecimal("2.125")));
    }

    @Test
    void validateProductQuantity_shouldUseDefaultPrecisionWhenUnitPrecisionMissing() {
        mockProductAndUnit(null);

        assertDoesNotThrow(() -> service.validateProductQuantity(1L, new BigDecimal("2.125")));
        assertThrows(ServiceException.class,
                () -> service.validateProductQuantity(1L, new BigDecimal("2.1255")));
    }

    @Test
    void validateProductQuantity_shouldRejectExcessDecimalPlaces() {
        mockProductAndUnit(0);

        assertThrows(ServiceException.class,
                () -> service.validateProductQuantity(1L, new BigDecimal("2.1")));
    }

    private void mockProductAndUnit(Integer quantityPrecision) {
        when(productService.getProduct(1L)).thenReturn(new ErpProductDO()
                .setId(1L).setName("测试产品").setUnitId(10L));
        when(productUnitService.getProductUnit(10L)).thenReturn(new ErpProductUnitDO()
                .setId(10L).setQuantityPrecision(quantityPrecision));
    }

}
