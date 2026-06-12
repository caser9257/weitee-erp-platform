package cn.iocoder.yudao.module.bpm.service.approval;

import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.statistics.ApprovalStatisticsRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.statistics.UserApprovalStatisticsRespVO;
import cn.iocoder.yudao.module.bpm.dal.mysql.approval.BpmApprovalInstanceSnapshotMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 审批统计报表 Service 实现类
 */
@Service
public class BpmApprovalStatisticsServiceImpl implements BpmApprovalStatisticsService {

    @Resource
    private BpmApprovalInstanceSnapshotMapper approvalInstanceSnapshotMapper;

    @Override
    public ApprovalStatisticsRespVO getApprovalStatistics() {
        // 使用 SQL 聚合查询各状态数量
        long totalCount = approvalInstanceSnapshotMapper.selectCountByStatus(1)
                + approvalInstanceSnapshotMapper.selectCountByStatus(2)
                + approvalInstanceSnapshotMapper.selectCountByStatus(3)
                + approvalInstanceSnapshotMapper.selectCountByStatus(4);
        long processingCount = approvalInstanceSnapshotMapper.selectCountByStatus(1);
        long approvedCount = approvalInstanceSnapshotMapper.selectCountByStatus(2);
        long rejectedCount = approvalInstanceSnapshotMapper.selectCountByStatus(3);
        long cancelledCount = approvalInstanceSnapshotMapper.selectCountByStatus(4);

        // 构建返回对象
        ApprovalStatisticsRespVO respVO = new ApprovalStatisticsRespVO();
        respVO.setTotalCount(totalCount);
        respVO.setProcessingCount(processingCount);
        respVO.setApprovedCount(approvedCount);
        respVO.setRejectedCount(rejectedCount);
        respVO.setCancelledCount(cancelledCount);

        return respVO;
    }

    @Override
    public UserApprovalStatisticsRespVO getUserApprovalStatistics(Long userId) {
        // 使用 SQL 聚合查询用户各状态数量
        long processingCount = approvalInstanceSnapshotMapper.selectCountByStartUserIdAndStatus(userId, 1);
        long approvedCount = approvalInstanceSnapshotMapper.selectCountByStartUserIdAndStatus(userId, 2);
        long rejectedCount = approvalInstanceSnapshotMapper.selectCountByStartUserIdAndStatus(userId, 3);
        long cancelledCount = approvalInstanceSnapshotMapper.selectCountByStartUserIdAndStatus(userId, 4);
        long totalCount = processingCount + approvedCount + rejectedCount + cancelledCount;

        // 构建返回对象
        UserApprovalStatisticsRespVO respVO = new UserApprovalStatisticsRespVO();
        respVO.setUserId(userId);
        respVO.setTotalCount(totalCount);
        respVO.setProcessingCount(processingCount);
        respVO.setApprovedCount(approvedCount);
        respVO.setRejectedCount(rejectedCount);
        respVO.setCancelledCount(cancelledCount);

        return respVO;
    }

    @Override
    public List<UserApprovalStatisticsRespVO> getAllUserApprovalStatistics() {
        // 使用 SQL 聚合查询按用户分组统计
        List<Map<String, Object>> groupResult = approvalInstanceSnapshotMapper.selectGroupByStartUserId();

        // 构建返回列表
        List<UserApprovalStatisticsRespVO> result = new ArrayList<>();
        for (Map<String, Object> row : groupResult) {
            UserApprovalStatisticsRespVO respVO = new UserApprovalStatisticsRespVO();
            respVO.setUserId(((Number) row.get("startUserId")).longValue());
            respVO.setTotalCount(((Number) row.get("totalCount")).longValue());
            respVO.setProcessingCount(((Number) row.get("processingCount")).longValue());
            respVO.setApprovedCount(((Number) row.get("approvedCount")).longValue());
            respVO.setRejectedCount(((Number) row.get("rejectedCount")).longValue());
            respVO.setCancelledCount(((Number) row.get("cancelledCount")).longValue());
            result.add(respVO);
        }

        return result;
    }

}
