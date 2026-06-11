package cn.iocoder.yudao.module.bpm.service.approval;

import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.statistics.ApprovalStatisticsRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.statistics.UserApprovalStatisticsRespVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalInstanceSnapshotDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.approval.BpmApprovalInstanceSnapshotMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 审批统计报表 Service 实现类
 */
@Service
public class BpmApprovalStatisticsServiceImpl implements BpmApprovalStatisticsService {

    @Resource
    private BpmApprovalInstanceSnapshotMapper approvalInstanceSnapshotMapper;

    @Override
    public ApprovalStatisticsRespVO getApprovalStatistics() {
        // 查询所有快照
        List<BpmApprovalInstanceSnapshotDO> snapshots = approvalInstanceSnapshotMapper.selectList();
        
        // 统计各状态数量
        long totalCount = snapshots.size();
        long processingCount = snapshots.stream()
                .filter(s -> s.getStatus() == 1)
                .count();
        long approvedCount = snapshots.stream()
                .filter(s -> s.getStatus() == 2)
                .count();
        long rejectedCount = snapshots.stream()
                .filter(s -> s.getStatus() == 3)
                .count();
        long cancelledCount = snapshots.stream()
                .filter(s -> s.getStatus() == 4)
                .count();
        
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
        // 查询用户的快照
        List<BpmApprovalInstanceSnapshotDO> snapshots = approvalInstanceSnapshotMapper.selectListByStartUserId(userId);
        
        // 统计各状态数量
        long totalCount = snapshots.size();
        long processingCount = snapshots.stream()
                .filter(s -> s.getStatus() == 1)
                .count();
        long approvedCount = snapshots.stream()
                .filter(s -> s.getStatus() == 2)
                .count();
        long rejectedCount = snapshots.stream()
                .filter(s -> s.getStatus() == 3)
                .count();
        long cancelledCount = snapshots.stream()
                .filter(s -> s.getStatus() == 4)
                .count();
        
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
        // 查询所有快照
        List<BpmApprovalInstanceSnapshotDO> snapshots = approvalInstanceSnapshotMapper.selectList();
        
        // 按用户分组
        Map<Long, List<BpmApprovalInstanceSnapshotDO>> userSnapshots = snapshots.stream()
                .collect(Collectors.groupingBy(s -> Long.parseLong(s.getCreator())));
        
        // 构建返回列表
        List<UserApprovalStatisticsRespVO> result = new ArrayList<>();
        for (Map.Entry<Long, List<BpmApprovalInstanceSnapshotDO>> entry : userSnapshots.entrySet()) {
            Long userId = entry.getKey();
            List<BpmApprovalInstanceSnapshotDO> userSnapshotList = entry.getValue();
            
            // 统计各状态数量
            long totalCount = userSnapshotList.size();
            long processingCount = userSnapshotList.stream()
                    .filter(s -> s.getStatus() == 1)
                    .count();
            long approvedCount = userSnapshotList.stream()
                    .filter(s -> s.getStatus() == 2)
                    .count();
            long rejectedCount = userSnapshotList.stream()
                    .filter(s -> s.getStatus() == 3)
                    .count();
            long cancelledCount = userSnapshotList.stream()
                    .filter(s -> s.getStatus() == 4)
                    .count();
            
            // 构建返回对象
            UserApprovalStatisticsRespVO respVO = new UserApprovalStatisticsRespVO();
            respVO.setUserId(userId);
            respVO.setTotalCount(totalCount);
            respVO.setProcessingCount(processingCount);
            respVO.setApprovedCount(approvedCount);
            respVO.setRejectedCount(rejectedCount);
            respVO.setCancelledCount(cancelledCount);
            
            result.add(respVO);
        }
        
        return result;
    }

}
