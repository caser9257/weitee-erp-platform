package cn.weitee.erp.module.bpm.dal.mysql.approval;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalScheduleDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

/**
 * 审批排班 Mapper
 */
@Mapper
public interface BpmApprovalScheduleMapper extends BaseMapperX<BpmApprovalScheduleDO> {

    /**
     * 根据用户 ID 和日期范围获取排班列表
     *
     * @param userId 用户 ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 排班列表
     */
    default List<BpmApprovalScheduleDO> selectListByUserIdAndDateRange(Long userId, Date startDate, Date endDate) {
        return selectList(new LambdaQueryWrapperX<BpmApprovalScheduleDO>()
                .eq(BpmApprovalScheduleDO::getUserId, userId)
                .ge(BpmApprovalScheduleDO::getScheduleDate, startDate)
                .le(BpmApprovalScheduleDO::getScheduleDate, endDate)
                .orderByAsc(BpmApprovalScheduleDO::getScheduleDate));
    }

    /**
     * 根据用户 ID 和日期获取排班
     *
     * @param userId 用户 ID
     * @param scheduleDate 排班日期
     * @return 排班信息
     */
    default BpmApprovalScheduleDO selectByUserIdAndDate(Long userId, Date scheduleDate) {
        return selectOne(new LambdaQueryWrapperX<BpmApprovalScheduleDO>()
                .eq(BpmApprovalScheduleDO::getUserId, userId)
                .eq(BpmApprovalScheduleDO::getScheduleDate, scheduleDate)
                .last("LIMIT 1"));
    }

}
