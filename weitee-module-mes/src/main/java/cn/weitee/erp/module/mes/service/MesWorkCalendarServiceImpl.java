package cn.weitee.erp.module.mes.service;

import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.mes.controller.admin.vo.workcalendar.MesWorkCalendarPageReqVO;
import cn.weitee.erp.module.mes.controller.admin.vo.workcalendar.MesWorkCalendarSaveReqVO;
import cn.weitee.erp.module.mes.dal.dataobject.MesWorkCalendarDO;
import cn.weitee.erp.module.mes.dal.mysql.MesWorkCalendarMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.regex.Pattern;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.mes.enums.ErrorCodeConstants.MES_WORK_CALENDAR_DATE_INVALID;
import static cn.weitee.erp.module.mes.enums.ErrorCodeConstants.MES_WORK_CALENDAR_NOT_EXISTS;
import static cn.weitee.erp.module.mes.enums.ErrorCodeConstants.MES_WORK_CALENDAR_WEEK_MASK_INVALID;

@Service
@Validated
public class MesWorkCalendarServiceImpl implements MesWorkCalendarService {

    private static final Pattern WEEK_MASK_PATTERN = Pattern.compile("^[01]{7}$");

    @Resource
    private MesWorkCalendarMapper mesWorkCalendarMapper;

    @Override
    public Long createCalendar(MesWorkCalendarSaveReqVO reqVO) {
        validateCalendar(reqVO);
        MesWorkCalendarDO calendar = BeanUtils.toBean(reqVO, MesWorkCalendarDO.class, item -> item.setStatus(1));
        mesWorkCalendarMapper.insert(calendar);
        return calendar.getId();
    }

    @Override
    public void updateCalendar(MesWorkCalendarSaveReqVO reqVO) {
        validateCalendarExists(reqVO.getId());
        validateCalendar(reqVO);
        mesWorkCalendarMapper.updateById(BeanUtils.toBean(reqVO, MesWorkCalendarDO.class));
    }

    @Override
    public void deleteCalendar(Long id) {
        validateCalendarExists(id);
        mesWorkCalendarMapper.deleteById(id);
    }

    @Override
    public MesWorkCalendarDO getCalendar(Long id) {
        return validateCalendarExists(id);
    }

    @Override
    public PageResult<MesWorkCalendarDO> getCalendarPage(MesWorkCalendarPageReqVO pageReqVO) {
        return mesWorkCalendarMapper.selectPage(pageReqVO);
    }

    private void validateCalendar(MesWorkCalendarSaveReqVO reqVO) {
        if (!WEEK_MASK_PATTERN.matcher(reqVO.getWeekMask()).matches()) {
            throw exception(MES_WORK_CALENDAR_WEEK_MASK_INVALID);
        }
        if (reqVO.getEffectiveDate() != null && reqVO.getExpireDate() != null
                && reqVO.getExpireDate().isBefore(reqVO.getEffectiveDate())) {
            throw exception(MES_WORK_CALENDAR_DATE_INVALID);
        }
    }

    private MesWorkCalendarDO validateCalendarExists(Long id) {
        MesWorkCalendarDO calendar = mesWorkCalendarMapper.selectById(id);
        if (calendar == null) {
            throw exception(MES_WORK_CALENDAR_NOT_EXISTS);
        }
        return calendar;
    }

}
