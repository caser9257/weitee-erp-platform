package cn.weitee.erp.module.erp.dal.mysql.product;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.erp.controller.admin.product.vo.unit.ErpProductUnitPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductUnitDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * ERP 产品单位 Mapper
 *
 * @author WeTai
 */
@Mapper
public interface ErpProductUnitMapper extends BaseMapperX<ErpProductUnitDO> {

    default PageResult<ErpProductUnitDO> selectPage(ErpProductUnitPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpProductUnitDO>()
                .likeIfPresent(ErpProductUnitDO::getName, reqVO.getName())
                .eqIfPresent(ErpProductUnitDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ErpProductUnitDO::getUnitType, reqVO.getUnitType())
                .eqIfPresent(ErpProductUnitDO::getBaseUnitId, reqVO.getBaseUnitId())
                .orderByDesc(ErpProductUnitDO::getId));
    }

    default ErpProductUnitDO selectByName(String name) {
        return selectOne(ErpProductUnitDO::getName, name);
    }

    default ErpProductUnitDO selectByNameAndUnitType(String name, Integer unitType) {
        return selectOne(new LambdaQueryWrapperX<ErpProductUnitDO>()
                .eq(ErpProductUnitDO::getName, name)
                .eq(ErpProductUnitDO::getUnitType, unitType));
    }

    default ErpProductUnitDO selectByNameAndBaseUnitId(String name, Long baseUnitId) {
        return selectOne(new LambdaQueryWrapperX<ErpProductUnitDO>()
                .eq(ErpProductUnitDO::getName, name)
                .eq(ErpProductUnitDO::getBaseUnitId, baseUnitId));
    }

    default List<ErpProductUnitDO> selectListByStatus(Integer status) {
        return selectList(ErpProductUnitDO::getStatus, status);
    }

    default List<ErpProductUnitDO> selectListByBaseUnitId(Long baseUnitId) {
        return selectList(ErpProductUnitDO::getBaseUnitId, baseUnitId);
    }

    default Long selectCountByBaseUnitId(Long baseUnitId) {
        return selectCount(ErpProductUnitDO::getBaseUnitId, baseUnitId);
    }

}