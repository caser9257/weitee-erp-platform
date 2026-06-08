package cn.iocoder.yudao.module.project.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.project.controller.admin.vo.comment.ProjectCommentCreateReqVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.comment.ProjectCommentPageReqVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.comment.ProjectCommentRespVO;
import cn.iocoder.yudao.module.project.service.comment.ProjectCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 项目评论")
@RestController
@RequestMapping("/project/comment")
@Validated
public class ProjectCommentController {

    @Resource
    private ProjectCommentService commentService;

    @PostMapping("/create")
    @Operation(summary = "创建评论")
    @PreAuthorize("@ss.hasPermission('project:comment:create')")
    public CommonResult<Long> createComment(@Valid @RequestBody ProjectCommentCreateReqVO reqVO) {
        return success(commentService.createComment(reqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除评论")
    @Parameter(name = "id", description = "评论编号", required = true)
    @PreAuthorize("@ss.hasPermission('project:comment:delete')")
    public CommonResult<Boolean> deleteComment(@RequestParam("id") Long id) {
        commentService.deleteComment(id);
        return success(true);
    }

    @PostMapping("/like")
    @Operation(summary = "点赞/取消点赞评论")
    @Parameter(name = "id", description = "评论编号", required = true)
    @PreAuthorize("@ss.hasPermission('project:comment:update')")
    public CommonResult<Boolean> toggleLike(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        commentService.toggleLike(id, userId);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得评论分页")
    @PreAuthorize("@ss.hasPermission('project:comment:query')")
    public CommonResult<PageResult<ProjectCommentRespVO>> getCommentPage(@Valid ProjectCommentPageReqVO pageReqVO) {
        return success(commentService.getCommentPage(pageReqVO));
    }

}
