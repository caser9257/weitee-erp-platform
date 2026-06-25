package cn.weitee.erp.module.system.controller.admin.postlevel;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.module.system.controller.admin.postlevel.vo.PostLevelAssignUsersReqVO;
import cn.weitee.erp.module.system.controller.admin.postlevel.vo.PostLevelDashboardRespVO;
import cn.weitee.erp.module.system.controller.admin.postlevel.vo.PostLevelDetailRespVO;
import cn.weitee.erp.module.system.controller.admin.postlevel.vo.PostLevelTreeNodeRespVO;
import cn.weitee.erp.module.system.controller.admin.postlevel.vo.PostOrgTreeRespVO;
import cn.weitee.erp.module.system.service.postlevel.PostLevelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin - Post level")
@RestController
@RequestMapping("/system/post-level")
@Validated
public class PostLevelController {

    @Resource
    private PostLevelService postLevelService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get post level dashboard")
    @PreAuthorize("@ss.hasPermission('system:post:query')")
    public CommonResult<PostLevelDashboardRespVO> getDashboard(@RequestParam(value = "deptId", required = false) Long deptId) {
        return success(postLevelService.getDashboard(deptId));
    }

    @GetMapping("/tree")
    @Operation(summary = "Get post level tree")
    @PreAuthorize("@ss.hasPermission('system:post:query')")
    public CommonResult<List<PostLevelTreeNodeRespVO>> getTree(@RequestParam(value = "deptId", required = false) Long deptId) {
        return success(postLevelService.getTree(deptId));
    }

    @GetMapping("/org-tree")
    @Operation(summary = "Get organization post tree")
    @PreAuthorize("@ss.hasPermission('system:post:query')")
    public CommonResult<PostOrgTreeRespVO> getOrgTree(@RequestParam(value = "deptId", required = false) Long deptId) {
        return success(postLevelService.getOrgTree(deptId));
    }

    @GetMapping("/detail")
    @Operation(summary = "Get post level detail")
    @PreAuthorize("@ss.hasPermission('system:post:query')")
    public CommonResult<PostLevelDetailRespVO> getDetail(@RequestParam("postId") Long postId) {
        return success(postLevelService.getDetail(postId));
    }

    @PostMapping("/assign-users")
    @Operation(summary = "Maintain post assignments")
    @PreAuthorize("@ss.hasPermission('system:post:update')")
    public CommonResult<Boolean> assignUsers(@Valid @RequestBody PostLevelAssignUsersReqVO reqVO) {
        postLevelService.assignUsers(reqVO);
        return success(true);
    }

}
