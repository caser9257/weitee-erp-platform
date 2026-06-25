package cn.weitee.erp.module.project.service.comment;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.project.controller.admin.vo.comment.ProjectCommentCreateReqVO;
import cn.weitee.erp.module.project.controller.admin.vo.comment.ProjectCommentPageReqVO;
import cn.weitee.erp.module.project.controller.admin.vo.comment.ProjectCommentRespVO;

public interface ProjectCommentService {

    /**
     * 创建评论
     */
    Long createComment(ProjectCommentCreateReqVO reqVO);

    /**
     * 删除评论
     */
    void deleteComment(Long id);

    /**
     * 点赞/取消点赞评论
     */
    void toggleLike(Long commentId, Long userId);

    /**
     * 获得评论分页
     */
    PageResult<ProjectCommentRespVO> getCommentPage(ProjectCommentPageReqVO reqVO);

}
