package cn.weitee.erp.module.erp.dal.mysql.stock;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.warehouse.category.ErpWarehouseCategoryListReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpWarehouseCategoryDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpWarehouseCategoryMapper extends BaseMapperX<ErpWarehouseCategoryDO> {

    default List<ErpWarehouseCategoryDO> selectList(ErpWarehouseCategoryListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<ErpWarehouseCategoryDO>()
                .likeIfPresent(ErpWarehouseCategoryDO::getName, reqVO.getName())
                .eqIfPresent(ErpWarehouseCategoryDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpWarehouseCategoryDO::getId));
    }

    default ErpWarehouseCategoryDO selectByParentIdAndName(Long parentId, String name) {
        return selectOne(ErpWarehouseCategoryDO::getParentId, parentId,
                ErpWarehouseCategoryDO::getName, name);
    }

    default ErpWarehouseCategoryDO selectByParentIdAndCode(Long parentId, String code) {
        return selectOne(ErpWarehouseCategoryDO::getParentId, parentId,
                ErpWarehouseCategoryDO::getCode, code);
    }

    default Long selectCountByParentId(Long parentId) {
        return selectCount(ErpWarehouseCategoryDO::getParentId, parentId);
    }

}
