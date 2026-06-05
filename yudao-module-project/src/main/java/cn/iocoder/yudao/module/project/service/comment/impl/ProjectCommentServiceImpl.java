package cn.iocoder.yudao.module.project.service.comment.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.project.controller.admin.vo.comment.ProjectCommentCreateReqVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.comment.ProjectCommentPageReqVO;
import cn.iocoder.yudao.module.project.controller.admin.vo.comment.ProjectCommentRespVO;
import cn.iocoder.yudao.module.project.dal.dataobject.comment.ProjectCommentDO;
import cn.iocoder.yudao.module.project.dal.dataobject.comment.ProjectCommentLikeDO;
import cn.iocoder.yudao.module.project.dal.dataobject.comment.ProjectCommentMentionDO;
import cn.iocoder.yudao.module.project.dal.mysql.comment.ProjectCommentLikeMapper;
import cn.iocoder.yudao.module.project.dal.mysql.comment.ProjectCommentMapper;
import cn.iocoder.yudao.module.project.dal.mysql.comment.ProjectCommentMentionMapper;
import cn.iocoder.yudao.module.project.service.comment.ProjectCommentService;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.project.enums.ErrorCodeConstants.*;

@Slf4j
@Service
public class ProjectCommentServiceImpl implements ProjectCommentService {

    @Resource
    private ProjectCommentMapper commentMapper;
    @Resource
    private ProjectCommentLikeMapper commentLikeMapper;
    @Resource
    private ProjectCommentMentionMapper commentMentionMapper;
    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createComment(ProjectCommentCreateReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();

        // 1. 创建评论
        ProjectCommentDO comment = ProjectCommentDO.builder()
                .projectId(reqVO.getProjectId())
                .taskId(reqVO.getTaskId())
                .parentId(reqVO.getParentId())
                .content(reqVO.getContent())
                .contentJson(reqVO.getContentJson())
                .userId(userId)
                .replyUserId(reqVO.getReplyUserId())
                .replyCommentId(reqVO.getReplyCommentId())
                .likeCount(0)
                .status(0)
                .build();
        commentMapper.insert(comment);

        // 2. 处理@提及
        if (reqVO.getMentionUserIds() != null && !reqVO.getMentionUserIds().isEmpty()) {
            for (Long mentionUserId : reqVO.getMentionUserIds()) {
                ProjectCommentMentionDO mention = ProjectCommentMentionDO.builder()
                        .commentId(comment.getId())
                        .userId(mentionUserId)
                        .notified(false)
                        .build();
                commentMentionMapper.insert(mention);
            }
            // 发送@提及通知
            sendMentionNotifications(comment, reqVO.getMentionUserIds());
        }

        // 3. 处理回复通知
        if (reqVO.getReplyUserId() != null) {
            // 发送回复通知
            sendReplyNotification(comment, reqVO.getReplyUserId());
        }

        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long id) {
        // 校验评论存在
        ProjectCommentDO comment = commentMapper.selectById(id);
        if (comment == null) {
            throw exception(COMMENT_NOT_FOUND);
        }

        // 软删除
        comment.setStatus(1);
        commentMapper.updateById(comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleLike(Long commentId, Long userId) {
        // 检查是否已点赞
        ProjectCommentLikeDO existingLike = commentLikeMapper.selectOne(
                new LambdaQueryWrapper<ProjectCommentLikeDO>()
                        .eq(ProjectCommentLikeDO::getCommentId, commentId)
                        .eq(ProjectCommentLikeDO::getUserId, userId));

        ProjectCommentDO comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw exception(COMMENT_NOT_FOUND);
        }

        if (existingLike != null) {
            // 取消点赞
            commentLikeMapper.deleteById(existingLike.getId());
            comment.setLikeCount(comment.getLikeCount() - 1);
        } else {
            // 点赞
            ProjectCommentLikeDO like = ProjectCommentLikeDO.builder()
                    .commentId(commentId)
                    .userId(userId)
                    .build();
            commentLikeMapper.insert(like);
            comment.setLikeCount(comment.getLikeCount() + 1);
        }
        commentMapper.updateById(comment);
    }

    @Override
    public PageResult<ProjectCommentRespVO> getCommentPage(ProjectCommentPageReqVO reqVO) {
        // 查询一级评论（parentId 为空）
        LambdaQueryWrapper<ProjectCommentDO> queryWrapper = new LambdaQueryWrapper<ProjectCommentDO>()
                .eq(reqVO.getProjectId() != null, ProjectCommentDO::getProjectId, reqVO.getProjectId())
                .eq(reqVO.getTaskId() != null, ProjectCommentDO::getTaskId, reqVO.getTaskId())
                .isNull(ProjectCommentDO::getParentId)
                .eq(ProjectCommentDO::getStatus, 0)
                .orderByDesc(ProjectCommentDO::getId);

        PageResult<ProjectCommentDO> pageResult = commentMapper.selectPage(reqVO, queryWrapper);
        List<ProjectCommentDO> commentList = pageResult.getList() == null ? Collections.emptyList() : pageResult.getList();

        // 收集所有需要查询的用户ID（评论人 + 回复目标用户）
        Set<Long> userIds = new HashSet<>();
        commentList.forEach(comment -> {
            userIds.add(comment.getUserId());
            if (comment.getReplyUserId() != null) {
                userIds.add(comment.getReplyUserId());
            }
        });

        // 批量查询用户信息
        Map<Long, AdminUserRespDTO> userMap = userIds.isEmpty() ? Collections.emptyMap() : adminUserApi.getUserMap(userIds);

        // 查询当前用户ID（用于判断点赞状态）
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();

        // 批量查询回复列表（每个一级评论最多3条回复）
        Map<Long, List<ProjectCommentDO>> repliesMap = new HashMap<>();
        if (!commentList.isEmpty()) {
            List<Long> parentIds = commentList.stream().map(ProjectCommentDO::getId).collect(Collectors.toList());
            LambdaQueryWrapper<ProjectCommentDO> repliesWrapper = new LambdaQueryWrapper<ProjectCommentDO>()
                    .in(ProjectCommentDO::getParentId, parentIds)
                    .eq(ProjectCommentDO::getStatus, 0)
                    .orderByAsc(ProjectCommentDO::getId);
            List<ProjectCommentDO> allReplies = commentMapper.selectList(repliesWrapper);

            // 收集回复中的用户ID
            allReplies.forEach(reply -> {
                userIds.add(reply.getUserId());
                if (reply.getReplyUserId() != null) {
                    userIds.add(reply.getReplyUserId());
                }
            });

            // 按 parentId 分组，每个最多取3条
            repliesMap = allReplies.stream()
                    .collect(Collectors.groupingBy(ProjectCommentDO::getParentId));
        }

        // 补充查询回复中新增的用户信息
        if (!userIds.isEmpty()) {
            Map<Long, AdminUserRespDTO> additionalUsers = adminUserApi.getUserMap(userIds);
            additionalUsers.forEach(userMap::putIfAbsent);
        }

        // 批量查询当前用户的点赞状态
        Set<Long> commentIds = commentList.stream().map(ProjectCommentDO::getId).collect(Collectors.toSet());
        Set<Long> likedCommentIds = new HashSet<>();
        if (!commentIds.isEmpty()) {
            LambdaQueryWrapper<ProjectCommentLikeDO> likeWrapper = new LambdaQueryWrapper<ProjectCommentLikeDO>()
                    .in(ProjectCommentLikeDO::getCommentId, commentIds)
                    .eq(ProjectCommentLikeDO::getUserId, currentUserId);
            List<ProjectCommentLikeDO> likes = commentLikeMapper.selectList(likeWrapper);
            likedCommentIds = likes.stream().map(ProjectCommentLikeDO::getCommentId).collect(Collectors.toSet());
        }

        // 转换为 RespVO
        Set<Long> finalLikedCommentIds = likedCommentIds;
        Map<Long, List<ProjectCommentDO>> finalRepliesMap = repliesMap;
        List<ProjectCommentRespVO> respList = commentList.stream()
                .map(comment -> convertToRespVO(comment, userMap, finalRepliesMap.getOrDefault(comment.getId(), Collections.emptyList()), finalLikedCommentIds))
                .collect(Collectors.toList());

        return new PageResult<>(respList, pageResult.getTotal());
    }

    private ProjectCommentRespVO convertToRespVO(ProjectCommentDO comment,
                                                  Map<Long, AdminUserRespDTO> userMap,
                                                  List<ProjectCommentDO> replies,
                                                  Set<Long> likedCommentIds) {
        ProjectCommentRespVO respVO = new ProjectCommentRespVO();
        respVO.setId(comment.getId());
        respVO.setProjectId(comment.getProjectId());
        respVO.setTaskId(comment.getTaskId());
        respVO.setParentId(comment.getParentId());
        respVO.setContent(comment.getContent());
        respVO.setContentJson(comment.getContentJson());
        respVO.setUserId(comment.getUserId());
        respVO.setReplyUserId(comment.getReplyUserId());
        respVO.setReplyCommentId(comment.getReplyCommentId());
        respVO.setLikeCount(comment.getLikeCount());
        respVO.setCreateTime(comment.getCreateTime());

        // 关联评论人信息
        AdminUserRespDTO user = userMap.get(comment.getUserId());
        respVO.setUserName(user != null ? user.getNickname() : "用户" + comment.getUserId());
        respVO.setUserAvatar(user != null ? user.getAvatar() : null);

        // 关联回复目标用户信息
        if (comment.getReplyUserId() != null) {
            AdminUserRespDTO replyUser = userMap.get(comment.getReplyUserId());
            respVO.setReplyUserName(replyUser != null ? replyUser.getNickname() : "用户" + comment.getReplyUserId());
        }

        // 当前用户是否已点赞
        respVO.setLikedByMe(likedCommentIds.contains(comment.getId()));

        // 查询回复列表（最多3条）
        if (replies != null && !replies.isEmpty()) {
            List<ProjectCommentRespVO> replyVOs = replies.stream()
                    .limit(3)
                    .map(reply -> convertToRespVO(reply, userMap, Collections.emptyList(), likedCommentIds))
                    .collect(Collectors.toList());
            respVO.setReplies(replyVOs);
        }

        // 查询@提及的用户列表
        LambdaQueryWrapper<ProjectCommentMentionDO> mentionWrapper = new LambdaQueryWrapper<ProjectCommentMentionDO>()
                .eq(ProjectCommentMentionDO::getCommentId, comment.getId());
        List<ProjectCommentMentionDO> mentions = commentMentionMapper.selectList(mentionWrapper);
        if (mentions != null && !mentions.isEmpty()) {
            List<ProjectCommentRespVO.MentionUserVO> mentionUsers = mentions.stream()
                    .map(mention -> {
                        ProjectCommentRespVO.MentionUserVO mentionVO = new ProjectCommentRespVO.MentionUserVO();
                        mentionVO.setUserId(mention.getUserId());
                        AdminUserRespDTO mentionUser = userMap.get(mention.getUserId());
                        mentionVO.setUserName(mentionUser != null ? mentionUser.getNickname() : "用户" + mention.getUserId());
                        mentionVO.setUserAvatar(mentionUser != null ? mentionUser.getAvatar() : null);
                        return mentionVO;
                    })
                    .collect(Collectors.toList());
            respVO.setMentionUsers(mentionUsers);
        }

        return respVO;
    }

    private void sendMentionNotifications(ProjectCommentDO comment, List<Long> mentionUserIds) {
        try {
            Long senderUserId = comment.getUserId();
            Map<String, Object> templateParams = new HashMap<>();
            templateParams.put("userName", "用户" + senderUserId);
            templateParams.put("taskName", "任务" + comment.getTaskId());
            templateParams.put("projectId", comment.getProjectId().toString());
            templateParams.put("taskId", comment.getTaskId().toString());

            for (Long mentionUserId : mentionUserIds) {
                NotifySendSingleToUserReqDTO reqDTO = new NotifySendSingleToUserReqDTO();
                reqDTO.setUserId(mentionUserId);
                reqDTO.setTemplateCode("project_comment_mention");
                reqDTO.setTemplateParams(templateParams);
                notifyMessageSendApi.sendSingleMessageToAdmin(reqDTO);
            }
        } catch (Exception e) {
            log.error("发送@提及通知失败", e);
        }
    }

    private void sendReplyNotification(ProjectCommentDO comment, Long replyUserId) {
        try {
            Long senderUserId = comment.getUserId();
            Map<String, Object> templateParams = new HashMap<>();
            templateParams.put("userName", "用户" + senderUserId);
            templateParams.put("taskName", "任务" + comment.getTaskId());
            templateParams.put("projectId", comment.getProjectId().toString());
            templateParams.put("taskId", comment.getTaskId().toString());

            NotifySendSingleToUserReqDTO reqDTO = new NotifySendSingleToUserReqDTO();
            reqDTO.setUserId(replyUserId);
            reqDTO.setTemplateCode("project_comment_reply");
            reqDTO.setTemplateParams(templateParams);
            notifyMessageSendApi.sendSingleMessageToAdmin(reqDTO);
        } catch (Exception e) {
            log.error("发送回复通知失败", e);
        }
    }

}
