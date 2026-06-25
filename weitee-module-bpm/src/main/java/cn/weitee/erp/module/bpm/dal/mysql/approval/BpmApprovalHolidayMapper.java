package cn.weitee.erp.module.bpm.dal.mysql.approval;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalHolidayDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

/**
 * 节假日 Mapper
 */
@Mapper
public interface BpmApprovalHolidayMapper extends BaseMapperX<BpmApprovalHolidayDO> {

    /**
     * 根据日期范围获取节假日列表
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 节假日列表
     */
    default List<BpmApprovalHolidayDO> selectListByDateRange(Date startDate, Date endDate) {
        return selectList(new LambdaQueryWrapperX<BpmApprovalHolidayDO>()
                .ge(BpmApprovalHolidayDO::getHolidayDate, startDate)
                .le(BpmApprovalHolidayDO::getHolidayDate, endDate)
                .orderByAsc(BpmApprovalHolidayDO::getHolidayDate));
    }

    /**
     * 根据日期获取节假日
     *
     * @param holidayDate 节假日日期
     * @return 节假日信息
     */
    default BpmApprovalHolidayDO selectByDate(Date holidayDate) {
        return selectOne(new LambdaQueryWrapperX<BpmApprovalHolidayDO>()
                .eq(BpmApprovalHolidayDO::getHolidayDate, holidayDate)
                .last("LIMIT 1"));
    }

    /**
     * 判断日期是否为节假日
     *
     * @param holidayDate 节假日日期（会自动截断时分秒，只比较日期部分）
     * @return 是否为节假日
     */
    default boolean isHoliday(Date holidayDate) {
        // 截断时分秒，只保留日期部分，避免 DATETIME vs DATE 比较精度问题
        Date dateOnly = cn.hutool.core.date.DateUtil.beginOfDay(holidayDate);
        BpmApprovalHolidayDO holiday = selectByDate(dateOnly);
        if (holiday == null) {
            return false;
        }
        // 如果是工作日（调休），则不是节假日
        if (Boolean.TRUE.equals(holiday.getIsWorkday())) {
            return false;
        }
        return true;
    }

}
