package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProcessRouteStepDO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpProcessRouteStepMapper extends BaseMapperX<ErpProcessRouteStepDO> {

    default void deleteByRouteId(Long routeId) {
        delete(ErpProcessRouteStepDO::getRouteId, routeId);
    }

    default List<ErpProcessRouteStepDO> selectListByRouteId(Long routeId) {
        return selectList(Wrappers.<ErpProcessRouteStepDO>lambdaQuery()
                .eq(ErpProcessRouteStepDO::getRouteId, routeId)
                .orderByAsc(ErpProcessRouteStepDO::getSort)
                .orderByAsc(ErpProcessRouteStepDO::getStepNo));
    }
}
