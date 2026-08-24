package cn.weitee.erp.module.mes.dal.mysql;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.mes.dal.dataobject.MesSopStepBindingDO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MesSopStepBindingMapper extends BaseMapperX<MesSopStepBindingDO> {

    default List<MesSopStepBindingDO> selectListBySopId(Long sopId) {
        return selectList(Wrappers.<MesSopStepBindingDO>lambdaQuery()
                .eq(MesSopStepBindingDO::getSopId, sopId));
    }

    default List<MesSopStepBindingDO> selectListByStepId(Long routeStepId) {
        return selectList(Wrappers.<MesSopStepBindingDO>lambdaQuery()
                .eq(MesSopStepBindingDO::getRouteStepId, routeStepId));
    }

    default void deleteBySopId(Long sopId) {
        delete(MesSopStepBindingDO::getSopId, sopId);
    }
}
