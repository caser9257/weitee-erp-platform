package cn.weitee.erp.module.mes.dal.mysql;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.mes.controller.admin.vo.workcalendar.MesWorkCalendarPageReqVO;
import cn.weitee.erp.module.mes.dal.dataobject.MesWorkCalendarDO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MesWorkCalendarMapper extends BaseMapperX<MesWorkCalendarDO> {

    default PageResult<MesWorkCalendarDO> selectPage(MesWorkCalendarPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesWorkCalendarDO>()
                .likeIfPresent(MesWorkCalendarDO::getCalendarName, reqVO.getCalendarName())
                .eqIfPresent(MesWorkCalendarDO::getWorkCenterId, reqVO.getWorkCenterId())
                .eqIfPresent(MesWorkCalendarDO::getStatus, reqVO.getStatus())
                .orderByDesc(MesWorkCalendarDO::getId));
    }

    /**
     * 查询启用中的日历：优先精确匹配工作中心，其次全局默认（work_center_id 为空）。
     */
    default List<MesWorkCalendarDO> selectEnabledList(Long workCenterId) {
        LambdaQueryWrapperX<MesWorkCalendarDO> wrapper = new LambdaQueryWrapperX<MesWorkCalendarDO>()
                .eq(MesWorkCalendarDO::getStatus, 1)
                .orderByDesc(MesWorkCalendarDO::getId);
        if (workCenterId != null) {
            wrapper.and(q -> q.eq(MesWorkCalendarDO::getWorkCenterId, workCenterId)
                    .or().isNull(MesWorkCalendarDO::getWorkCenterId));
        } else {
            wrapper.isNull(MesWorkCalendarDO::getWorkCenterId);
        }
        return selectList(wrapper);
    }
}
