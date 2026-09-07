package cn.weitee.erp.module.erp.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.exception.ErrorCode;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductUnitDO;
import cn.weitee.erp.module.erp.enums.product.ErpProductUnitTypeEnum;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_UNIT_BASE_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_UNIT_BASE_QUANTITY_PRECISION_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_UNIT_INPUT_QUANTITY_PRECISION_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_UNIT_NOT_BELONG_TO_BASE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_UNIT_NOT_ENABLE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_UNIT_NOT_EXISTS;

/**
 * ERP 单据明细录入单位换算 Service 实现类
 */
@Service
@Validated
public class ErpProductUnitConversionServiceImpl implements ErpProductUnitConversionService {

    private static final int DEFAULT_QUANTITY_PRECISION = 3;

    @Resource
    private ErpProductService productService;
    @Resource
    private ErpProductUnitService productUnitService;

    @Override
    public ConversionResult convert(Long productId, Long inputUnitId, BigDecimal inputCount) {
        return convertBatch(Collections.singletonList(new ConversionRequest(productId, inputUnitId, inputCount)))
                .get(0);
    }

    @Override
    public List<ConversionResult> convertBatch(List<ConversionRequest> requests) {
        if (CollUtil.isEmpty(requests)) {
            return Collections.emptyList();
        }
        // 1. 批量查询产品（同时校验产品存在与启用）
        Map<Long, ErpProductDO> productMap = convertMap(
                productService.validProductList(convertSet(requests, ConversionRequest::getProductId)),
                ErpProductDO::getId);
        // 2. 批量查询涉及的全部单位：产品基本单位 + 录入单位
        Set<Long> unitIds = new HashSet<>(convertSet(productMap.values(), ErpProductDO::getUnitId));
        unitIds.addAll(convertSet(requests, ConversionRequest::getInputUnitId));
        Map<Long, ErpProductUnitDO> unitMap = productUnitService.getProductUnitMap(unitIds);
        // 3. 逐条换算
        return convertList(requests, request -> doConvert(request,
                productMap.get(request.getProductId()), unitMap));
    }

    private ConversionResult doConvert(ConversionRequest request, ErpProductDO product,
                                       Map<Long, ErpProductUnitDO> unitMap) {
        ErpProductUnitDO baseUnit = unitMap.get(product.getUnitId());
        if (baseUnit == null) {
            throw exception(PRODUCT_UNIT_NOT_EXISTS);
        }
        if (request.getInputCount() == null) {
            return new ConversionResult(baseUnit.getId(), null, null, null);
        }
        // 1. 基本单位录入：数量即记账数量，仅校验精度
        if (request.getInputUnitId() == null || request.getInputUnitId().equals(baseUnit.getId())) {
            validateUnitQuantity(baseUnit, product.getName(), request.getInputCount(),
                    PRODUCT_UNIT_INPUT_QUANTITY_PRECISION_INVALID);
            return new ConversionResult(baseUnit.getId(), request.getInputCount(), null, request.getInputCount());
        }
        // 2. 辅助单位录入：校验单位存在、启用、属于该基本单位的单位族
        ErpProductUnitDO inputUnit = unitMap.get(request.getInputUnitId());
        if (inputUnit == null) {
            throw exception(PRODUCT_UNIT_NOT_EXISTS);
        }
        if (!CommonStatusEnum.isEnable(inputUnit.getStatus())) {
            throw exception(PRODUCT_UNIT_NOT_ENABLE, inputUnit.getName());
        }
        if (!ErpProductUnitTypeEnum.AUXILIARY.getType().equals(inputUnit.getUnitType())
                || !baseUnit.getId().equals(inputUnit.getBaseUnitId())) {
            throw exception(PRODUCT_UNIT_NOT_BELONG_TO_BASE, inputUnit.getName(), baseUnit.getName());
        }
        if (inputUnit.getConversionRate() == null) {
            throw exception(PRODUCT_UNIT_BASE_NOT_EXISTS);
        }
        validateUnitQuantity(inputUnit, product.getName(), request.getInputCount(),
                PRODUCT_UNIT_INPUT_QUANTITY_PRECISION_INVALID);
        // 3. 换算为基本单位数量，并校验满足基本单位精度，避免产生无法记账的碎片数量
        BigDecimal baseCount = request.getInputCount().multiply(inputUnit.getConversionRate());
        validateUnitQuantity(baseUnit, product.getName(), baseCount,
                PRODUCT_UNIT_BASE_QUANTITY_PRECISION_INVALID);
        return new ConversionResult(inputUnit.getId(), request.getInputCount(),
                inputUnit.getConversionRate(), baseCount);
    }

    private void validateUnitQuantity(ErpProductUnitDO unit, String productName, BigDecimal quantity,
                                      ErrorCode errorCode) {
        int precision = unit.getQuantityPrecision() != null
                ? unit.getQuantityPrecision() : DEFAULT_QUANTITY_PRECISION;
        if (quantity.stripTrailingZeros().scale() > precision) {
            throw exception(errorCode, productName, unit.getName(), precision);
        }
    }

}
