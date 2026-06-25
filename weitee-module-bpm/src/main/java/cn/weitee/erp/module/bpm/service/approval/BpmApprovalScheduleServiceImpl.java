package cn.weitee.erp.module.bpm.service.approval;

import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalHolidayDO;
import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalScheduleDO;
import cn.weitee.erp.module.bpm.dal.mysql.approval.BpmApprovalHolidayMapper;
import cn.weitee.erp.module.bpm.dal.mysql.approval.BpmApprovalScheduleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 审批排班 Service 实现类
 */
@Service
@Validated
public class BpmApprovalScheduleServiceImpl implements BpmApprovalScheduleService {

    @Resource
    private BpmApprovalScheduleMapper approvalScheduleMapper;

    @Resource
    private BpmApprovalHolidayMapper approvalHolidayMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSchedule(BpmApprovalScheduleDO schedule) {
        approvalScheduleMapper.insert(schedule);
        return schedule.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSchedule(BpmApprovalScheduleDO schedule) {
        approvalScheduleMapper.updateById(schedule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSchedule(Long id) {
        approvalScheduleMapper.deleteById(id);
    }

    @Override
    public List<BpmApprovalScheduleDO> getSchedules(Long userId, Date startDate, Date endDate) {
        return approvalScheduleMapper.selectListByUserIdAndDateRange(userId, startDate, endDate);
    }

    @Override
    public boolean isWorkday(Long userId, Date date) {
        // 1. 检查是否为节假日
        if (approvalHolidayMapper.isHoliday(date)) {
            return false;
        }

        // 2. 检查用户排班
        BpmApprovalScheduleDO schedule = approvalScheduleMapper.selectByUserIdAndDate(userId, date);
        if (schedule != null) {
            // 如果有排班记录，根据排班类型判断
            return "WORK".equals(schedule.getScheduleType());
        }

        // 3. 默认判断：周一到周五为工作日
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY;
    }

    @Override
    public Date getNextWorkday(Long userId, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        // 最多查找 30 天
        for (int i = 0; i < 30; i++) {
            if (isWorkday(userId, calendar.getTime())) {
                return calendar.getTime();
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // 如果找不到工作日，返回原日期
        return date;
    }

}
