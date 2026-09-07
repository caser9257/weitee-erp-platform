package cn.weitee.erp.module.erp.service.product;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.product.vo.unit.ErpProductUnitPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.unit.ErpProductUnitSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductUnitDO;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * ERP 产品单位 Service 接口
 *
 * @author WeTai
 */
public interface ErpProductUnitService {

    /**
     * 创建产品单位
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProductUnit(@Valid ErpProductUnitSaveReqVO createReqVO);

    /**
     * 更新产品单位
     *
     * @param updateReqVO 更新信息
     */
    void updateProductUnit(@Valid ErpProductUnitSaveReqVO updateReqVO);

    /**
     * 删除产品单位
     *
     * @param id 编号
     */
    void deleteProductUnit(Long id);

    /**
     * 获得产品单位
     *
     * @param id 编号
     * @return 产品单位
     */
    ErpProductUnitDO getProductUnit(Long id);

    /**
     * 获得产品单位分页
     *
     * @param pageReqVO 分页查询
     * @return 产品单位分页
     */
    PageResult<ErpProductUnitDO> getProductUnitPage(ErpProductUnitPageReqVO pageReqVO);

    /**
     * 获得指定状态的产品单位列表
     *
     * @param status 状态
     * @return 产品单位列表
     */
    List<ErpProductUnitDO> getProductUnitListByStatus(Integer status);

    /**
     * 获得产品单位列表
     *
     * @param ids 编号数组
     * @return 产品单位列表
     */
    List<ErpProductUnitDO> getProductUnitList(Collection<Long> ids);

    /**
     * 获得产品单位 Map
     *
     * @param ids 编号数组
     * @return 产品单位 Map
     */
    default Map<Long, ErpProductUnitDO> getProductUnitMap(Collection<Long> ids) {
        return convertMap(getProductUnitList(ids), ErpProductUnitDO::getId);
    }

    /**
     * 获得指定基本单位下的辅助单位列表
     *
     * @param baseUnitId 基本单位编号
     * @return 辅助单位列表
     */
    List<ErpProductUnitDO> getProductUnitListByBaseUnitId(Long baseUnitId);

    /**
     * 校验单位存在且处于启用状态
     *
     * @param id 单位编号
     * @return 产品单位
     */
    ErpProductUnitDO validateProductUnitEnabled(Long id);

    /**
     * 校验录入单位属于产品基本单位的单位族（即基本单位本身，或指向它的启用辅助单位）
     *
     * @param unitId     录入单位编号
     * @param baseUnitId 产品基本单位编号
     * @return 录入单位
     */
    ErpProductUnitDO validateUnitBelongsToBase(Long unitId, Long baseUnitId);

    /**
     * 将指定单位的数量换算为基本单位数量
     *
     * @param unitId   单位编号（基本单位时原样返回）
     * @param quantity 该单位的数量
     * @return 基本单位数量
     */
    BigDecimal toBaseUnitQuantity(Long unitId, BigDecimal quantity);

    /**
     * 将基本单位数量换算为指定单位的数量
     *
     * @param unitId       单位编号（基本单位时原样返回）
     * @param baseQuantity 基本单位数量
     * @return 该单位的数量
     */
    BigDecimal fromBaseUnitQuantity(Long unitId, BigDecimal baseQuantity);

}