package cn.iocoder.yudao.module.bpm.controller.admin.approval;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.delegation.BpmApprovalDelegationPageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.delegation.BpmApprovalDelegationRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.delegation.BpmApprovalDelegationSaveReqVO;
import cn.iocoder.yudao.module.bpm.service.approval.BpmApprovalDelegationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 审批委托")
@RestController
@RequestMapping("/bpm/approval-delegation")
@Validated
public class BpmApprovalDelegationController {

    @Resource
    private BpmApprovalDelegationService approvalDelegationService;

    @PostMapping("/create")
    @Operation(summary = "创建委托")
    @PreAuthorize("@ss.hasPermission('bpm:approval-delegation:create')")
    public CommonResult<Long> createDelegation(@Valid @RequestBody BpmApprovalDelegationSaveReqVO reqVO) {
        return success(approvalDelegationService.createDelegation(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新委托")
    @PreAuthorize("@ss.hasPermission('bpm:approval-delegation:update')")
    public CommonResult<Boolean> updateDelegation(@Valid @RequestBody BpmApprovalDelegationSaveReqVO reqVO) {
        approvalDelegationService.updateDelegation(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除委托")
    @Parameter(name = "id", description = "委托编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('bpm:approval-delegation:delete')")
    public CommonResult<Boolean> deleteDelegation(@RequestParam("id") Long id) {
        approvalDelegationService.deleteDelegation(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取委托详情")
    @Parameter(name = "id", description = "委托编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('bpm:approval-delegation:query')")
    public CommonResult<BpmApprovalDelegationRespVO> getDelegation(@RequestParam("id") Long id) {
        return success(approvalDelegationService.getDelegation(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获取委托分页列表")
    @PreAuthorize("@ss.hasPermission('bpm:approval-delegation:query')")
    public CommonResult<PageResult<BpmApprovalDelegationRespVO>> getDelegationPage(@Valid BpmApprovalDelegationPageReqVO reqVO) {
        return success(approvalDelegationService.getDelegationPage(reqVO));
    }

    @GetMapping("/my-valid")
    @Operation(summary = "获取我的有效委托")
    @PreAuthorize("@ss.hasPermission('bpm:approval-delegation:query')")
    public CommonResult<List<BpmApprovalDelegationRespVO>> getMyValidDelegations() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(approvalDelegationService.getValidDelegations(userId));
    }

}
