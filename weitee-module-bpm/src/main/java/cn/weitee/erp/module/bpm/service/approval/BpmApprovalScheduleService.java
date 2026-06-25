package cn.weitee.erp.module.bpm.service.approval;

import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalScheduleDO;

import java.util.Date;
import java.util.List;

/**
 * 审批排班 Service 接口
 */
public interface BpmApprovalScheduleService {

    /**
     * 创建排班
     *
     * @param schedule 排班信息
     * @return 排班 ID
     */
    Long createSchedule(BpmApprovalScheduleDO schedule);

    /**
     * 更新排班
     *
     * @param schedule 排班信息
     */
    void updateSchedule(BpmApprovalScheduleDO schedule);

    /**
     * 删除排班
     *
     * @param id 排班 ID
     */
    void deleteSchedule(Long id);

    /**
     * 获取用户的排班列表
     *
     * @param userId 用户 ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 排班列表
     */
    List<BpmApprovalScheduleDO> getSchedules(Long userId, Date startDate, Date endDate);

    /**
     * 判断用户在指定日期是否为工作日
     *
     * @param userId 用户 ID
     * @param date 日期
     * @return 是否为工作日
     */
    boolean isWorkday(Long userId, Date date);

    /**
     * 获取用户的下一个工作日
     *
     * @param userId 用户 ID
     * @param date 起始日期
     * @return 下一个工作日
     */
    Date getNextWorkday(Long userId, Date date);

}
