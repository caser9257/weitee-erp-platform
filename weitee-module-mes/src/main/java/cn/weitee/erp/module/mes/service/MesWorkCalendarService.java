package cn.weitee.erp.module.mes.service;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.mes.controller.admin.vo.workcalendar.MesWorkCalendarPageReqVO;
import cn.weitee.erp.module.mes.controller.admin.vo.workcalendar.MesWorkCalendarSaveReqVO;
import cn.weitee.erp.module.mes.dal.dataobject.MesWorkCalendarDO;

public interface MesWorkCalendarService {

    Long createCalendar(MesWorkCalendarSaveReqVO reqVO);

    void updateCalendar(MesWorkCalendarSaveReqVO reqVO);

    void deleteCalendar(Long id);

    MesWorkCalendarDO getCalendar(Long id);

    PageResult<MesWorkCalendarDO> getCalendarPage(MesWorkCalendarPageReqVO pageReqVO);

}
