package cn.weitee.erp.module.erp.service.product;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.controller.admin.product.vo.unit.ErpProductUnitSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductUnitDO;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductUnitMapper;
import cn.weitee.erp.module.erp.enums.product.ErpProductUnitTypeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_UNIT_BASE_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_UNIT_BASE_REQUIRED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_UNIT_CONVERSION_RATE_REQUIRED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_UNIT_EXITS_AUXILIARY;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_UNIT_NOT_BELONG_TO_BASE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_UNIT_USED_BY_PRODUCT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErpProductUnitServiceImplTest {

    @Mock
    private ErpProductUnitMapper erpProductUnitMapper;
    @Mock
    private ErpProductService productService;
    @InjectMocks
    private ErpProductUnitServiceImpl service;

    private static final Integer BASE = ErpProductUnitTypeEnum.BASE.getType();
    private static final Integer AUX = ErpProductUnitTypeEnum.AUXILIARY.getType();

    @Test
    void createProductUnit_base_shouldClearConversionFields() {
        ErpProductUnitSaveReqVO reqVO = new ErpProductUnitSaveReqVO();
        reqVO.setName("个");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setUnitType(BASE);
        reqVO.setQuantityPrecision(0);
        // 基本单位即使误传换算字段，也必须被强制清空
        reqVO.setBaseUnitId(99L);
        reqVO.setConversionRate(new BigDecimal("12"));

        service.createProductUnit(reqVO);

        assertNull(reqVO.getBaseUnitId());
        assertNull(reqVO.getConversionRate());
        verify(erpProductUnitMapper).insert(any(ErpProductUnitDO.class));
    }

    @Test
    void createProductUnit_auxiliaryWithoutBase_shouldThrow() {
        ErpProductUnitSaveReqVO reqVO = buildAuxiliary("箱", null, new BigDecimal("12"));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createProductUnit(reqVO));
        assertEquals(PRODUCT_UNIT_BASE_REQUIRED.getCode(), ex.getCode());
    }

    @Test
    void createProductUnit_auxiliaryWithoutRate_shouldThrow() {
        ErpProductUnitSaveReqVO reqVO = buildAuxiliary("箱", 10L, null);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createProductUnit(reqVO));
        assertEquals(PRODUCT_UNIT_CONVERSION_RATE_REQUIRED.getCode(), ex.getCode());
    }

    @Test
    void createProductUnit_auxiliaryBaseNotEnabled_shouldThrow() {
        when(erpProductUnitMapper.selectById(10L)).thenReturn(new ErpProductUnitDO()
                .setId(10L).setName("个").setUnitType(BASE)
                .setStatus(CommonStatusEnum.DISABLE.getStatus()));
        ErpProductUnitSaveReqVO reqVO = buildAuxiliary("箱", 10L, new BigDecimal("12"));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createProductUnit(reqVO));
        assertEquals(PRODUCT_UNIT_BASE_NOT_EXISTS.getCode(), ex.getCode());
    }

    @Test
    void createProductUnit_auxiliaryBaseIsAuxiliary_shouldThrow() {
        // 单层换算：禁止把辅助单位挂到另一个辅助单位下
        when(erpProductUnitMapper.selectById(10L)).thenReturn(new ErpProductUnitDO()
                .setId(10L).setName("打").setUnitType(AUX).setBaseUnitId(1L)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        ErpProductUnitSaveReqVO reqVO = buildAuxiliary("箱", 10L, new BigDecimal("12"));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createProductUnit(reqVO));
        assertEquals(PRODUCT_UNIT_BASE_NOT_EXISTS.getCode(), ex.getCode());
    }

    @Test
    void updateProductUnit_baseToAuxiliary_usedByProduct_shouldThrow() {
        when(erpProductUnitMapper.selectById(10L)).thenReturn(new ErpProductUnitDO()
                .setId(10L).setName("个").setUnitType(BASE)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(erpProductUnitMapper.selectById(20L)).thenReturn(new ErpProductUnitDO()
                .setId(20L).setName("米").setUnitType(BASE)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(productService.getProductCountByUnitId(10L)).thenReturn(1L);
        ErpProductUnitSaveReqVO reqVO = buildAuxiliary("个", 20L, new BigDecimal("1"));
        reqVO.setId(10L);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.updateProductUnit(reqVO));
        assertEquals(PRODUCT_UNIT_USED_BY_PRODUCT.getCode(), ex.getCode());
    }

    @Test
    void deleteProductUnit_baseWithAuxiliary_shouldThrow() {
        when(erpProductUnitMapper.selectById(10L)).thenReturn(new ErpProductUnitDO()
                .setId(10L).setName("个").setUnitType(BASE)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(productService.getProductCountByUnitId(10L)).thenReturn(0L);
        when(erpProductUnitMapper.selectCountByBaseUnitId(10L)).thenReturn(2L);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.deleteProductUnit(10L));
        assertEquals(PRODUCT_UNIT_EXITS_AUXILIARY.getCode(), ex.getCode());
        verify(erpProductUnitMapper, never()).deleteById(any());
    }

    @Test
    void validateUnitBelongsToBase_auxiliaryOfBase_shouldPass() {
        when(erpProductUnitMapper.selectById(20L)).thenReturn(new ErpProductUnitDO()
                .setId(20L).setName("箱").setUnitType(AUX).setBaseUnitId(10L)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));

        ErpProductUnitDO unit = service.validateUnitBelongsToBase(20L, 10L);
        assertEquals(20L, unit.getId());
    }

    @Test
    void validateUnitBelongsToBase_unrelatedUnit_shouldThrow() {
        when(erpProductUnitMapper.selectById(30L)).thenReturn(new ErpProductUnitDO()
                .setId(30L).setName("kg").setUnitType(BASE)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(erpProductUnitMapper.selectById(10L)).thenReturn(new ErpProductUnitDO()
                .setId(10L).setName("个").setUnitType(BASE)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.validateUnitBelongsToBase(30L, 10L));
        assertEquals(PRODUCT_UNIT_NOT_BELONG_TO_BASE.getCode(), ex.getCode());
    }

    @Test
    void toBaseUnitQuantity_shouldMultiplyRateForAuxiliary() {
        when(erpProductUnitMapper.selectById(20L)).thenReturn(buildBoxUnit());

        BigDecimal result = service.toBaseUnitQuantity(20L, new BigDecimal("3"));
        assertEquals(0, new BigDecimal("36").compareTo(result));
    }

    @Test
    void toBaseUnitQuantity_baseUnit_shouldReturnSame() {
        when(erpProductUnitMapper.selectById(10L)).thenReturn(new ErpProductUnitDO()
                .setId(10L).setName("个").setUnitType(BASE)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));

        assertEquals(0, new BigDecimal("3").compareTo(service.toBaseUnitQuantity(10L, new BigDecimal("3"))));
    }

    @Test
    void fromBaseUnitQuantity_shouldDivideRateForAuxiliary() {
        when(erpProductUnitMapper.selectById(20L)).thenReturn(buildBoxUnit());

        BigDecimal result = service.fromBaseUnitQuantity(20L, new BigDecimal("36"));
        assertEquals(0, new BigDecimal("3").compareTo(result));
    }

    @Test
    void createProductUnit_auxiliarySameNameAsBaseUnitAllowed() {
        // 场景：已经存在名为"箱"的基本单位，新建归属于"个"的辅助单位"箱"（1箱=10个），不应冲突
        when(erpProductUnitMapper.selectById(10L)).thenReturn(new ErpProductUnitDO()
                .setId(10L).setName("个").setUnitType(BASE)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(erpProductUnitMapper.selectByNameAndBaseUnitId("箱", 10L)).thenReturn(null);

        ErpProductUnitSaveReqVO reqVO = buildAuxiliary("箱", 10L, new BigDecimal("10"));
        service.createProductUnit(reqVO);

        verify(erpProductUnitMapper).insert(any(ErpProductUnitDO.class));
    }

    @Test
    void createProductUnit_auxiliarySameNameAsItsOwnBaseUnit_shouldThrow() {
        // 场景：辅助单位不能与所属的基本单位同名（如基本单位为"箱"，辅助单位也叫"箱"，产生 1 箱 = 10 箱 的语义歧义）
        when(erpProductUnitMapper.selectById(10L)).thenReturn(new ErpProductUnitDO()
                .setId(10L).setName("箱").setUnitType(BASE)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));

        ErpProductUnitSaveReqVO reqVO = buildAuxiliary("箱", 10L, new BigDecimal("10"));
        ServiceException ex = assertThrows(ServiceException.class, () -> service.createProductUnit(reqVO));
        assertEquals(cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_UNIT_NAME_DUPLICATE.getCode(), ex.getCode());
    }

    private ErpProductUnitDO buildBoxUnit() {
        return new ErpProductUnitDO().setId(20L).setName("箱").setUnitType(AUX)
                .setBaseUnitId(10L).setConversionRate(new BigDecimal("12"))
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    private ErpProductUnitSaveReqVO buildAuxiliary(String name, Long baseUnitId, BigDecimal rate) {
        ErpProductUnitSaveReqVO reqVO = new ErpProductUnitSaveReqVO();
        reqVO.setName(name);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setUnitType(AUX);
        reqVO.setBaseUnitId(baseUnitId);
        reqVO.setConversionRate(rate);
        reqVO.setQuantityPrecision(2);
        return reqVO;
    }

}
