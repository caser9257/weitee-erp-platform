package cn.weitee.erp.module.erp.service.product;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductUnitDO;
import cn.weitee.erp.module.erp.enums.product.ErpProductUnitTypeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_UNIT_BASE_QUANTITY_PRECISION_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_UNIT_INPUT_QUANTITY_PRECISION_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_UNIT_NOT_BELONG_TO_BASE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_UNIT_NOT_ENABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErpProductUnitConversionServiceImplTest {

    @Mock
    private ErpProductService productService;
    @Mock
    private ErpProductUnitService productUnitService;
    @InjectMocks
    private ErpProductUnitConversionServiceImpl service;

    private static final Long PRODUCT_ID = 1L;
    private static final Long BASE_UNIT_ID = 10L;   // 个，精度 0
    private static final Long BOX_UNIT_ID = 20L;    // 箱，1 箱 = 12 个，精度 2
    private static final Long OTHER_UNIT_ID = 30L;  // kg，与产品无关

    private void mockProductsAndUnits(ErpProductUnitDO... extraUnits) {
        when(productService.validProductList(anyCollection())).thenReturn(Collections.singletonList(
                new ErpProductDO().setId(PRODUCT_ID).setName("测试产品").setUnitId(BASE_UNIT_ID)));
        List<ErpProductUnitDO> units = new java.util.ArrayList<>();
        units.add(buildBaseUnit());
        units.add(buildBoxUnit());
        units.add(buildOtherUnit());
        units.addAll(Arrays.asList(extraUnits));
        when(productUnitService.getProductUnitMap(anyCollection())).thenReturn(
                cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap(
                        units, ErpProductUnitDO::getId));
    }

    @Test
    void convert_nullUnit_shouldPassThroughAsBase() {
        mockProductsAndUnits();

        ErpProductUnitConversionService.ConversionResult result =
                service.convert(PRODUCT_ID, null, new BigDecimal("5"));
        assertEquals(BASE_UNIT_ID, result.getInputUnitId());
        assertNull(result.getConversionRate());
        assertEquals(0, new BigDecimal("5").compareTo(result.getBaseCount()));
    }

    @Test
    void convert_auxiliaryUnit_shouldConvertAndSnapshotRate() {
        mockProductsAndUnits();

        ErpProductUnitConversionService.ConversionResult result =
                service.convert(PRODUCT_ID, BOX_UNIT_ID, new BigDecimal("3"));
        assertEquals(BOX_UNIT_ID, result.getInputUnitId());
        assertEquals(0, new BigDecimal("12").compareTo(result.getConversionRate()));
        assertEquals(0, new BigDecimal("36").compareTo(result.getBaseCount()));
        assertEquals(0, new BigDecimal("3").compareTo(result.getInputCount()));
    }

    @Test
    void convert_auxiliaryNegativeCount_shouldConvert() {
        mockProductsAndUnits();

        ErpProductUnitConversionService.ConversionResult result =
                service.convert(PRODUCT_ID, BOX_UNIT_ID, new BigDecimal("-2"));
        assertEquals(0, new BigDecimal("-24").compareTo(result.getBaseCount()));
    }

    @Test
    void convert_inputPrecisionExceeded_shouldThrow() {
        mockProductsAndUnits();

        // 箱精度 2，录入 1.234 箱超限
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.convert(PRODUCT_ID, BOX_UNIT_ID, new BigDecimal("1.234")));
        assertEquals(PRODUCT_UNIT_INPUT_QUANTITY_PRECISION_INVALID.getCode(), ex.getCode());
    }

    @Test
    void convert_basePrecisionExceeded_shouldThrow() {
        mockProductsAndUnits();

        // 0.5 箱 = 6 个 OK；0.04 箱 = 0.48 个，基本单位精度 0 超限
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.convert(PRODUCT_ID, BOX_UNIT_ID, new BigDecimal("0.04")));
        assertEquals(PRODUCT_UNIT_BASE_QUANTITY_PRECISION_INVALID.getCode(), ex.getCode());
    }

    @Test
    void convert_unitNotBelongToBase_shouldThrow() {
        mockProductsAndUnits();

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.convert(PRODUCT_ID, OTHER_UNIT_ID, new BigDecimal("1")));
        assertEquals(PRODUCT_UNIT_NOT_BELONG_TO_BASE.getCode(), ex.getCode());
    }

    @Test
    void convert_disabledUnit_shouldThrow() {
        when(productService.validProductList(anyCollection())).thenReturn(Collections.singletonList(
                new ErpProductDO().setId(PRODUCT_ID).setName("测试产品").setUnitId(BASE_UNIT_ID)));
        when(productUnitService.getProductUnitMap(anyCollection())).thenReturn(
                buildUnitMap(buildBaseUnit(), buildDisabledBoxUnit()));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.convert(PRODUCT_ID, BOX_UNIT_ID, new BigDecimal("1")));
        assertEquals(PRODUCT_UNIT_NOT_ENABLE.getCode(), ex.getCode());
    }

    @Test
    void convert_nullCount_shouldReturnEmptyResult() {
        mockProductsAndUnits();

        ErpProductUnitConversionService.ConversionResult result =
                service.convert(PRODUCT_ID, BOX_UNIT_ID, null);
        assertEquals(BASE_UNIT_ID, result.getInputUnitId());
        assertNull(result.getBaseCount());
    }

    private ErpProductUnitDO buildBaseUnit() {
        return new ErpProductUnitDO().setId(BASE_UNIT_ID).setName("个")
                .setUnitType(ErpProductUnitTypeEnum.BASE.getType())
                .setStatus(CommonStatusEnum.ENABLE.getStatus()).setQuantityPrecision(0);
    }

    private ErpProductUnitDO buildBoxUnit() {
        return new ErpProductUnitDO().setId(BOX_UNIT_ID).setName("箱")
                .setUnitType(ErpProductUnitTypeEnum.AUXILIARY.getType())
                .setBaseUnitId(BASE_UNIT_ID).setConversionRate(new BigDecimal("12"))
                .setStatus(CommonStatusEnum.ENABLE.getStatus()).setQuantityPrecision(2);
    }

    private ErpProductUnitDO buildDisabledBoxUnit() {
        return buildBoxUnit().setStatus(CommonStatusEnum.DISABLE.getStatus());
    }

    private ErpProductUnitDO buildOtherUnit() {
        return new ErpProductUnitDO().setId(OTHER_UNIT_ID).setName("kg")
                .setUnitType(ErpProductUnitTypeEnum.BASE.getType())
                .setStatus(CommonStatusEnum.ENABLE.getStatus()).setQuantityPrecision(3);
    }

    private java.util.Map<Long, ErpProductUnitDO> buildUnitMap(ErpProductUnitDO... units) {
        java.util.Map<Long, ErpProductUnitDO> map = new java.util.HashMap<>();
        for (ErpProductUnitDO unit : units) {
            map.put(unit.getId(), unit);
        }
        return map;
    }

}
