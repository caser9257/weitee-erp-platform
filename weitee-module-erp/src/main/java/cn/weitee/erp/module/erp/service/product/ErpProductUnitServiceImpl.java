package cn.weitee.erp.module.erp.service.product;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.product.vo.unit.ErpProductUnitPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.unit.ErpProductUnitSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductUnitDO;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductUnitMapper;
import cn.weitee.erp.module.erp.enums.product.ErpProductUnitTypeEnum;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.*;

/**
 * ERP 产品单位 Service 实现类
 *
 * @author WeTai
 */
@Service
@Validated
public class ErpProductUnitServiceImpl implements ErpProductUnitService {

    @Resource
    private ErpProductUnitMapper erpProductUnitMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private ErpProductService productService;

    /**
     * 换算展示数量的小数位（fromBaseUnitQuantity 除法精度）
     */
    private static final int CONVERT_DISPLAY_SCALE = 6;

    @Override
    public Long createProductUnit(ErpProductUnitSaveReqVO createReqVO) {
        // 1. 校验换算字段（若为基本单位会在此清空换算字段，若为辅助单位会校验 baseUnit 存在且有效）
        validateProductUnitConversion(null, createReqVO);
        // 2. 校验名字唯一（基本单位在基本单位中唯一；辅助单位在同基准单位中唯一且不与基准单位同名）
        validateProductUnitNameUnique(null, createReqVO.getName(), createReqVO.getUnitType(), createReqVO.getBaseUnitId());
        // 3. 插入
        ErpProductUnitDO unit = BeanUtils.toBean(createReqVO, ErpProductUnitDO.class);
        erpProductUnitMapper.insert(unit);
        return unit.getId();
    }

    @Override
    public void updateProductUnit(ErpProductUnitSaveReqVO updateReqVO) {
        // 1.1 校验存在
        ErpProductUnitDO oldUnit = validateProductUnitExists(updateReqVO.getId());
        // 1.2 校验换算字段
        validateProductUnitConversion(oldUnit, updateReqVO);
        // 1.3 校验名字唯一
        validateProductUnitNameUnique(updateReqVO.getId(), updateReqVO.getName(), updateReqVO.getUnitType(), updateReqVO.getBaseUnitId());
        // 1.4 基本单位改为辅助单位时，必须未被产品记账引用、未被辅助单位引用，避免破坏单层换算
        if (ErpProductUnitTypeEnum.BASE.getType().equals(oldUnit.getUnitType())
                && ErpProductUnitTypeEnum.AUXILIARY.getType().equals(updateReqVO.getUnitType())) {
            if (productService.getProductCountByUnitId(updateReqVO.getId()) > 0) {
                throw exception(PRODUCT_UNIT_USED_BY_PRODUCT);
            }
            if (erpProductUnitMapper.selectCountByBaseUnitId(updateReqVO.getId()) > 0) {
                throw exception(PRODUCT_UNIT_EXITS_AUXILIARY);
            }
        }
        // 2. 更新
        ErpProductUnitDO updateObj = BeanUtils.toBean(updateReqVO, ErpProductUnitDO.class);
        erpProductUnitMapper.updateById(updateObj);
    }

    /**
     * 校验单位类型与换算字段的合法性，并对基本单位强制清空换算字段
     *
     * @param oldUnit 更新时的原单位，创建时为 null
     * @param reqVO   请求
     */
    @VisibleForTesting
    void validateProductUnitConversion(ErpProductUnitDO oldUnit, ErpProductUnitSaveReqVO reqVO) {
        if (ErpProductUnitTypeEnum.BASE.getType().equals(reqVO.getUnitType())) {
            // 基本单位不允许携带换算关系，强制清空，避免脏数据
            reqVO.setBaseUnitId(null);
            reqVO.setConversionRate(null);
            return;
        }
        // 辅助单位：必须指定基本单位与换算率
        if (reqVO.getBaseUnitId() == null) {
            throw exception(PRODUCT_UNIT_BASE_REQUIRED);
        }
        if (reqVO.getConversionRate() == null) {
            throw exception(PRODUCT_UNIT_CONVERSION_RATE_REQUIRED);
        }
        if (reqVO.getBaseUnitId().equals(reqVO.getId())) {
            throw exception(PRODUCT_UNIT_BASE_SELF_REFERENCE);
        }
        // 基本单位必须存在、是基本单位、且启用（单层换算，禁止挂到辅助单位下）
        ErpProductUnitDO baseUnit = erpProductUnitMapper.selectById(reqVO.getBaseUnitId());
        if (baseUnit == null
                || !ErpProductUnitTypeEnum.BASE.getType().equals(baseUnit.getUnitType())
                || !CommonStatusEnum.isEnable(baseUnit.getStatus())) {
            throw exception(PRODUCT_UNIT_BASE_NOT_EXISTS);
        }
    }

    @VisibleForTesting
    void validateProductUnitNameUnique(Long id, String name, Integer unitType, Long baseUnitId) {
        // 1. 基本单位：在所有基本单位（unitType = 0）范围内保证名字唯一
        if (ErpProductUnitTypeEnum.BASE.getType().equals(unitType)) {
            ErpProductUnitDO existBase = erpProductUnitMapper.selectByNameAndUnitType(name, ErpProductUnitTypeEnum.BASE.getType());
            if (existBase != null && (id == null || !existBase.getId().equals(id))) {
                throw exception(PRODUCT_UNIT_NAME_DUPLICATE);
            }
            return;
        }

        // 2. 辅助单位：
        // 2.1 辅助单位的名字不能与所属基本单位同名（避免如“1 箱 = 10 箱”等语义歧义）
        if (baseUnitId != null) {
            ErpProductUnitDO baseUnit = erpProductUnitMapper.selectById(baseUnitId);
            if (baseUnit != null && name.trim().equalsIgnoreCase(baseUnit.getName().trim())) {
                throw exception(PRODUCT_UNIT_NAME_DUPLICATE);
            }
            // 2.2 在相同基本单位归属下，辅助单位名字唯一
            ErpProductUnitDO existAux = erpProductUnitMapper.selectByNameAndBaseUnitId(name, baseUnitId);
            if (existAux != null && (id == null || !existAux.getId().equals(id))) {
                throw exception(PRODUCT_UNIT_NAME_DUPLICATE);
            }
        }
    }

    @Override
    public void deleteProductUnit(Long id) {
        // 1.1 校验存在
        ErpProductUnitDO unit = validateProductUnitExists(id);
        // 1.2 校验产品是否使用
        if (productService.getProductCountByUnitId(id) > 0) {
            throw exception(PRODUCT_UNIT_EXITS_PRODUCT);
        }
        // 1.3 基本单位被辅助单位引用时，禁止删除（避免换算关系悬空）
        if (ErpProductUnitTypeEnum.BASE.getType().equals(unit.getUnitType())
                && erpProductUnitMapper.selectCountByBaseUnitId(id) > 0) {
            throw exception(PRODUCT_UNIT_EXITS_AUXILIARY);
        }
        // 2. 删除
        erpProductUnitMapper.deleteById(id);
    }

    private ErpProductUnitDO validateProductUnitExists(Long id) {
        ErpProductUnitDO unit = erpProductUnitMapper.selectById(id);
        if (unit == null) {
            throw exception(PRODUCT_UNIT_NOT_EXISTS);
        }
        return unit;
    }

    @Override
    public ErpProductUnitDO getProductUnit(Long id) {
        return erpProductUnitMapper.selectById(id);
    }

    @Override
    public PageResult<ErpProductUnitDO> getProductUnitPage(ErpProductUnitPageReqVO pageReqVO) {
        return erpProductUnitMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpProductUnitDO> getProductUnitListByStatus(Integer status) {
        return erpProductUnitMapper.selectListByStatus(status);
    }

    @Override
    public List<ErpProductUnitDO> getProductUnitList(Collection<Long> ids) {
         return erpProductUnitMapper.selectByIds(ids);
    }

    @Override
    public List<ErpProductUnitDO> getProductUnitListByBaseUnitId(Long baseUnitId) {
        return erpProductUnitMapper.selectListByBaseUnitId(baseUnitId);
    }

    @Override
    public ErpProductUnitDO validateProductUnitEnabled(Long id) {
        ErpProductUnitDO unit = validateProductUnitExists(id);
        if (!CommonStatusEnum.isEnable(unit.getStatus())) {
            throw exception(PRODUCT_UNIT_NOT_EXISTS);
        }
        return unit;
    }

    @Override
    public ErpProductUnitDO validateUnitBelongsToBase(Long unitId, Long baseUnitId) {
        ErpProductUnitDO unit = validateProductUnitEnabled(unitId);
        if (unit.getId().equals(baseUnitId)) {
            return unit;
        }
        if (ErpProductUnitTypeEnum.AUXILIARY.getType().equals(unit.getUnitType())
                && baseUnitId != null && baseUnitId.equals(unit.getBaseUnitId())) {
            return unit;
        }
        ErpProductUnitDO baseUnit = getProductUnit(baseUnitId);
        throw exception(PRODUCT_UNIT_NOT_BELONG_TO_BASE, unit.getName(),
                baseUnit != null ? baseUnit.getName() : baseUnitId);
    }

    @Override
    public BigDecimal toBaseUnitQuantity(Long unitId, BigDecimal quantity) {
        if (quantity == null) {
            return null;
        }
        ErpProductUnitDO unit = validateProductUnitEnabled(unitId);
        if (unit.getConversionRate() == null) {
            return quantity;
        }
        return quantity.multiply(unit.getConversionRate());
    }

    @Override
    public BigDecimal fromBaseUnitQuantity(Long unitId, BigDecimal baseQuantity) {
        if (baseQuantity == null) {
            return null;
        }
        ErpProductUnitDO unit = validateProductUnitEnabled(unitId);
        if (unit.getConversionRate() == null) {
            return baseQuantity;
        }
        return baseQuantity.divide(unit.getConversionRate(), CONVERT_DISPLAY_SCALE, RoundingMode.HALF_UP);
    }

}
