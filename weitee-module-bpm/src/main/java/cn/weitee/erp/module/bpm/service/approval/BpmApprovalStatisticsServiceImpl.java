package cn.weitee.erp.module.bpm.service.approval;

import cn.weitee.erp.module.bpm.controller.admin.approval.vo.statistics.ApprovalStatisticsRespVO;
import cn.weitee.erp.module.bpm.controller.admin.approval.vo.statistics.UserApprovalStatisticsRespVO;
import cn.weitee.erp.module.bpm.dal.mysql.approval.BpmApprovalInstanceSnapshotMapper;
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
        Map<String, Object> stats = approvalInstanceSnapshotMapper.selectStatusStatistics();
        return buildStatisticsResp(stats);
    }

    @Override
    public ApprovalStatisticsRespVO getCurrentApprovalStatistics() {
        Map<String, Object> stats = approvalInstanceSnapshotMapper.selectEffectiveStatusStatistics();
        return buildStatisticsResp(stats);
    }

    @Override
    public UserApprovalStatisticsRespVO getUserApprovalStatistics(Long userId) {
        Map<String, Object> stats = approvalInstanceSnapshotMapper.selectStatusStatisticsByStartUserId(userId);
        return buildUserStatisticsResp(userId, stats);
    }

    @Override
    public List<UserApprovalStatisticsRespVO> getAllUserApprovalStatistics() {
        List<Map<String, Object>> groupResult = approvalInstanceSnapshotMapper.selectGroupByStartUserId();
        return buildUserStatisticsList(groupResult);
    }

    @Override
    public List<UserApprovalStatisticsRespVO> getAllCurrentUserApprovalStatistics() {
        List<Map<String, Object>> groupResult = approvalInstanceSnapshotMapper.selectEffectiveGroupByStartUserId();
        return buildUserStatisticsList(groupResult);
    }

    private ApprovalStatisticsRespVO buildStatisticsResp(Map<String, Object> stats) {
        long processingCount = ((Number) stats.get("processingCount")).longValue();
        long approvedCount = ((Number) stats.get("approvedCount")).longValue();
        long rejectedCount = ((Number) stats.get("rejectedCount")).longValue();
        long cancelledCount = ((Number) stats.get("cancelledCount")).longValue();

        ApprovalStatisticsRespVO respVO = new ApprovalStatisticsRespVO();
        respVO.setTotalCount(processingCount + approvedCount + rejectedCount + cancelledCount);
        respVO.setProcessingCount(processingCount);
        respVO.setApprovedCount(approvedCount);
        respVO.setRejectedCount(rejectedCount);
        respVO.setCancelledCount(cancelledCount);

        return respVO;
    }

    private UserApprovalStatisticsRespVO buildUserStatisticsResp(Long userId, Map<String, Object> stats) {
        long processingCount = ((Number) stats.get("processingCount")).longValue();
        long approvedCount = ((Number) stats.get("approvedCount")).longValue();
        long rejectedCount = ((Number) stats.get("rejectedCount")).longValue();
        long cancelledCount = ((Number) stats.get("cancelledCount")).longValue();

        UserApprovalStatisticsRespVO respVO = new UserApprovalStatisticsRespVO();
        respVO.setUserId(userId);
        respVO.setTotalCount(processingCount + approvedCount + rejectedCount + cancelledCount);
        respVO.setProcessingCount(processingCount);
        respVO.setApprovedCount(approvedCount);
        respVO.setRejectedCount(rejectedCount);
        respVO.setCancelledCount(cancelledCount);

        return respVO;
    }

    private List<UserApprovalStatisticsRespVO> buildUserStatisticsList(List<Map<String, Object>> groupResult) {
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
