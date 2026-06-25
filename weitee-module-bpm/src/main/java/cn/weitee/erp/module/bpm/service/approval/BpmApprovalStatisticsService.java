package cn.weitee.erp.module.bpm.service.approval;

import cn.weitee.erp.module.bpm.controller.admin.approval.vo.statistics.ApprovalStatisticsRespVO;
import cn.weitee.erp.module.bpm.controller.admin.approval.vo.statistics.UserApprovalStatisticsRespVO;

import java.util.List;

/**
 * 审批统计报表 Service 接口
 */
public interface BpmApprovalStatisticsService {

    /**
     * 获取审批统计数据
     *
     * @return 审批统计数据
     */
    ApprovalStatisticsRespVO getApprovalStatistics();

    /**
     * 获取用户审批统计数据
     *
     * @param userId 用户 ID
     * @return 用户审批统计数据
     */
    UserApprovalStatisticsRespVO getUserApprovalStatistics(Long userId);

    /**
     * 获取所有用户的审批统计数据
     *
     * @return 用户审批统计数据列表
     */
    List<UserApprovalStatisticsRespVO> getAllUserApprovalStatistics();

}
