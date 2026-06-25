package cn.weitee.erp.module.bpm.controller.admin.approval;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.module.bpm.controller.admin.approval.vo.statistics.ApprovalStatisticsRespVO;
import cn.weitee.erp.module.bpm.controller.admin.approval.vo.statistics.UserApprovalStatisticsRespVO;
import cn.weitee.erp.module.bpm.service.approval.BpmApprovalStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

/**
 * 审批统计 Controller
 *
 * @author system
 */
@Tag(name = "BPM - 审批统计")
@RestController
@RequestMapping("/bpm/approval/statistics")
@Slf4j
public class BpmApprovalStatisticsController {

    @Resource
    private BpmApprovalStatisticsService approvalStatisticsService;

    @GetMapping("/summary")
    @Operation(summary = "获取审批统计概览")
    @PreAuthorize("@ss.hasPermission('bpm:approval:query')")
    public CommonResult<ApprovalStatisticsRespVO> getApprovalStatistics() {
        return success(approvalStatisticsService.getApprovalStatistics());
    }

    @GetMapping("/user-list")
    @Operation(summary = "获取所有用户的审批统计")
    @PreAuthorize("@ss.hasPermission('bpm:approval:query')")
    public CommonResult<List<UserApprovalStatisticsRespVO>> getAllUserApprovalStatistics() {
        return success(approvalStatisticsService.getAllUserApprovalStatistics());
    }

}
