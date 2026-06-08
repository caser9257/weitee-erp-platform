package cn.iocoder.yudao.module.bpm.controller.admin.approval;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.scene.BpmApprovalScenePageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.scene.BpmApprovalSceneRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.scene.BpmApprovalSceneSaveReqVO;
import cn.iocoder.yudao.module.bpm.service.approval.BpmApprovalSceneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 审批场景")
@RestController
@RequestMapping("/bpm/approval-scene")
@Validated
public class BpmApprovalSceneController {

    @Resource
    private BpmApprovalSceneService approvalSceneService;

    @GetMapping("/page")
    @Operation(summary = "获取审批场景分页")
    @PreAuthorize("@ss.hasPermission('bpm:approval-scene:query')")
    public CommonResult<PageResult<BpmApprovalSceneRespVO>> getScenePage(@Valid BpmApprovalScenePageReqVO pageReqVO) {
        return success(approvalSceneService.getScenePage(pageReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获取审批场景详情")
    @Parameter(name = "id", description = "场景编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('bpm:approval-scene:query')")
    public CommonResult<BpmApprovalSceneRespVO> getScene(@RequestParam("id") Long id) {
        return success(approvalSceneService.getScene(id));
    }

    @GetMapping("/get-by-code")
    @Operation(summary = "根据场景编码获取审批场景")
    @Parameter(name = "sceneCode", description = "场景编码", required = true, example = "erp.finance.payment.submit")
    @PreAuthorize("@ss.hasPermission('bpm:approval-scene:query')")
    public CommonResult<BpmApprovalSceneRespVO> getSceneByCode(@RequestParam("sceneCode") String sceneCode) {
        return success(approvalSceneService.getSceneByCode(sceneCode));
    }

    @PostMapping("/create")
    @Operation(summary = "创建审批场景")
    @PreAuthorize("@ss.hasPermission('bpm:approval-scene:create')")
    public CommonResult<Long> createScene(@Valid @RequestBody BpmApprovalSceneSaveReqVO createReqVO) {
        return success(approvalSceneService.createScene(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新审批场景")
    @PreAuthorize("@ss.hasPermission('bpm:approval-scene:update')")
    public CommonResult<Boolean> updateScene(@Valid @RequestBody BpmApprovalSceneSaveReqVO updateReqVO) {
        approvalSceneService.updateScene(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除审批场景")
    @Parameter(name = "id", description = "场景编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('bpm:approval-scene:delete')")
    public CommonResult<Boolean> deleteScene(@RequestParam("id") Long id) {
        approvalSceneService.deleteScene(id);
        return success(true);
    }

    @PutMapping("/enable")
    @Operation(summary = "启用审批场景")
    @Parameter(name = "id", description = "场景编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('bpm:approval-scene:update')")
    public CommonResult<Boolean> enableScene(@RequestParam("id") Long id) {
        approvalSceneService.updateSceneStatus(id, 1);
        return success(true);
    }

    @PutMapping("/disable")
    @Operation(summary = "禁用审批场景")
    @Parameter(name = "id", description = "场景编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('bpm:approval-scene:update')")
    public CommonResult<Boolean> disableScene(@RequestParam("id") Long id) {
        approvalSceneService.updateSceneStatus(id, 0);
        return success(true);
    }

}
