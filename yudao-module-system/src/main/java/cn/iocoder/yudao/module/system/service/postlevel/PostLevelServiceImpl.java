package cn.iocoder.yudao.module.system.service.postlevel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptListReqVO;
import cn.iocoder.yudao.module.system.controller.admin.postlevel.vo.PostLevelAssignUserItemReqVO;
import cn.iocoder.yudao.module.system.controller.admin.postlevel.vo.PostLevelAssignUsersReqVO;
import cn.iocoder.yudao.module.system.controller.admin.postlevel.vo.PostLevelAssignedUserRespVO;
import cn.iocoder.yudao.module.system.controller.admin.postlevel.vo.PostLevelChangeLogRespVO;
import cn.iocoder.yudao.module.system.controller.admin.postlevel.vo.PostLevelDashboardRespVO;
import cn.iocoder.yudao.module.system.controller.admin.postlevel.vo.PostLevelDetailRespVO;
import cn.iocoder.yudao.module.system.controller.admin.postlevel.vo.PostLevelSummaryRespVO;
import cn.iocoder.yudao.module.system.controller.admin.postlevel.vo.PostLevelTreeNodeRespVO;
import cn.iocoder.yudao.module.system.controller.admin.postlevel.vo.PostOrgTreeNodeRespVO;
import cn.iocoder.yudao.module.system.controller.admin.postlevel.vo.PostOrgTreeRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.PostDO;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.UserPostDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.dept.DeptMapper;
import cn.iocoder.yudao.module.system.dal.mysql.dept.PostMapper;
import cn.iocoder.yudao.module.system.dal.mysql.dept.UserPostMapper;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.POST_NOT_ENABLE;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.POST_NOT_FOUND;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_IS_DISABLE;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_NOT_EXISTS;

@Service
public class PostLevelServiceImpl implements PostLevelService {

    private static final String ROOT_LABEL = "\u516c\u53f8";
    private static final String LEVEL_HIGH = "\u9ad8\u7ea7";
    private static final String LEVEL_MIDDLE = "\u4e2d\u7ea7";
    private static final String LEVEL_PRIMARY = "\u521d\u7ea7";
    private static final String LEVEL_UNCLASSIFIED = "\u672a\u5206\u7ea7";
    private static final List<String> LEVEL_ORDER = List.of(LEVEL_HIGH, LEVEL_MIDDLE, LEVEL_PRIMARY, LEVEL_UNCLASSIFIED);

    @Resource
    private DeptMapper deptMapper;
    @Resource
    private PostMapper postMapper;
    @Resource
    private UserPostMapper userPostMapper;
    @Resource
    private AdminUserMapper adminUserMapper;

    @Override
    public PostLevelDashboardRespVO getDashboard(Long deptId) {
        ScopeData scopeData = loadScopeData(deptId);
        int staffQuota = scopeData.posts.stream().mapToInt(post -> safeInt(post.getStaffQuota())).sum();
        int assignedUserCount = countAssignedUserTotal(scopeData.userPostsByPostId);

        PostLevelDashboardRespVO respVO = new PostLevelDashboardRespVO();
        respVO.setPostCount(scopeData.posts.size());
        respVO.setAssignedUserCount(assignedUserCount);
        respVO.setVacancyCount(Math.max(staffQuota - assignedUserCount, 0));
        respVO.setKeyPostCount((int) scopeData.posts.stream().filter(post -> Boolean.TRUE.equals(post.getKeyPosition())).count());
        respVO.setPartTimeUserCount(countPartTimeUsers(scopeData.userPosts));
        respVO.setNoBackupCount(0);
        respVO.setLevelSummaries(buildLevelSummaries(scopeData.posts, scopeData.userPostsByPostId));
        respVO.setRiskTips(Collections.emptyList());
        return respVO;
    }

    @Override
    public List<PostLevelTreeNodeRespVO> getTree(Long deptId) {
        ScopeData scopeData = loadScopeData(deptId);
        Map<Long, AdminUserDO> userMap = getUserMap(scopeData.userPosts.stream()
                .map(UserPostDO::getUserId)
                .collect(Collectors.toSet()));
        Map<String, List<PostDO>> groupedPosts = scopeData.posts.stream()
                .collect(Collectors.groupingBy(this::resolveLevel, LinkedHashMap::new, Collectors.toList()));

        PostLevelTreeNodeRespVO root = new PostLevelTreeNodeRespVO();
        root.setId(buildLevelNodeId(deptId, ROOT_LABEL));
        root.setLabel(resolveScopeLabel(scopeData, deptId));
        root.setType("level");
        root.setStaffQuota(scopeData.posts.stream().mapToInt(post -> safeInt(post.getStaffQuota())).sum());
        root.setAssignedUserCount(countAssignedUserTotal(scopeData.userPostsByPostId));
        root.setVacancyCount(Math.max(safeInt(root.getStaffQuota()) - safeInt(root.getAssignedUserCount()), 0));

        List<PostLevelTreeNodeRespVO> levelNodes = new ArrayList<>();
        for (String level : orderedLevels(groupedPosts.keySet())) {
            List<PostDO> levelPosts = groupedPosts.getOrDefault(level, Collections.emptyList());
            PostLevelTreeNodeRespVO levelNode = new PostLevelTreeNodeRespVO();
            levelNode.setId(buildLevelNodeId(deptId, level));
            levelNode.setLabel(level);
            levelNode.setType("level");
            levelNode.setLevel(level);
            levelNode.setStaffQuota(levelPosts.stream().mapToInt(post -> safeInt(post.getStaffQuota())).sum());
            levelNode.setAssignedUserCount(levelPosts.stream()
                    .mapToInt(post -> countAssignedUser(scopeData.userPostsByPostId.get(post.getId())))
                    .sum());
            levelNode.setVacancyCount(Math.max(safeInt(levelNode.getStaffQuota()) - safeInt(levelNode.getAssignedUserCount()), 0));
            levelNode.setChildren(levelPosts.stream()
                    .sorted(Comparator.comparing(PostDO::getSort, Comparator.nullsLast(Integer::compareTo))
                            .thenComparing(PostDO::getId))
                    .map(post -> buildPostLevelNode(post, scopeData.userPostsByPostId.get(post.getId()), userMap))
                    .collect(Collectors.toList()));
            levelNodes.add(levelNode);
        }
        root.setChildren(levelNodes);
        return Collections.singletonList(root);
    }

    @Override
    public PostOrgTreeRespVO getOrgTree(Long deptId) {
        ScopeData scopeData = loadScopeData(deptId);
        Map<Long, List<DeptDO>> childDeptMap = scopeData.depts.stream()
                .collect(Collectors.groupingBy(DeptDO::getParentId));
        Map<Long, List<PostDO>> deptPostsMap = scopeData.posts.stream()
                .collect(Collectors.groupingBy(PostDO::getDeptId));
        Map<Long, DeptAggregate> aggregateMap = buildDeptAggregateMap(scopeData.depts, scopeData.posts, scopeData.userPostsByPostId);

        List<DeptDO> rootDepts = scopeData.depts.stream()
                .filter(dept -> isRootDept(dept, scopeData.deptIds))
                .sorted(Comparator.comparing(DeptDO::getSort, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(DeptDO::getId))
                .collect(Collectors.toList());

        List<PostOrgTreeNodeRespVO> tree = rootDepts.stream()
                .map(dept -> buildDeptOrgNode(dept, 1, childDeptMap, deptPostsMap, aggregateMap, scopeData.userPostsByPostId,
                        scopeData.deptMap, scopeData.deptIds))
                .map(this::pruneEmptyOrgNode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        PostOrgTreeRespVO respVO = new PostOrgTreeRespVO();
        respVO.setDeptCount(countVisibleDeptNodes(tree));
        respVO.setPostCount(countVisiblePostNodes(tree));
        respVO.setStaffQuota(sumVisibleStaffQuota(tree));
        respVO.setAssignedUserCount(sumVisibleAssignedUsers(tree));
        respVO.setMaxDepth(calculateDeptDepth(tree));
        respVO.setTree(tree);
        return respVO;
    }

    @Override
    public PostLevelDetailRespVO getDetail(Long postId) {
        PostDO post = postMapper.selectById(postId);
        if (post == null) {
            PostLevelDetailRespVO empty = new PostLevelDetailRespVO();
            empty.setRiskTags(Collections.emptyList());
            empty.setAssignedUsers(Collections.emptyList());
            empty.setChangeLogs(Collections.emptyList());
            return empty;
        }

        DeptDO dept = post.getDeptId() == null ? null : deptMapper.selectById(post.getDeptId());
        List<UserPostDO> userPosts = sortUserPosts(userPostMapper.selectListByPostId(postId));
        Map<Long, AdminUserDO> userMap = getUserMap(userPosts.stream()
                .map(UserPostDO::getUserId)
                .collect(Collectors.toSet()));

        PostLevelDetailRespVO respVO = new PostLevelDetailRespVO();
        respVO.setPostId(post.getId());
        respVO.setCode(post.getCode());
        respVO.setName(post.getName());
        respVO.setLevel(resolveLevel(post));
        respVO.setDeptId(post.getDeptId());
        respVO.setDeptName(dept == null ? null : dept.getName());
        respVO.setStatus(post.getStatus());
        respVO.setSort(post.getSort());
        respVO.setStaffQuota(safeInt(post.getStaffQuota()));
        respVO.setKeyPosition(Boolean.TRUE.equals(post.getKeyPosition()));
        respVO.setAllowPartTime(Boolean.TRUE.equals(post.getAllowPartTime()));
        respVO.setJobDescription(post.getJobDescription());
        respVO.setRemark(post.getRemark());
        respVO.setAssignedUserCount(userPosts.size());
        respVO.setPrimaryUserCount((int) userPosts.stream().filter(item -> Boolean.TRUE.equals(item.getIsPrimary())).count());
        respVO.setVacancyCount(Math.max(safeInt(post.getStaffQuota()) - userPosts.size(), 0));
        respVO.setRiskTags(Collections.emptyList());
        respVO.setAssignedUsers(userPosts.stream()
                .map(item -> buildAssignedUser(item, userMap.get(item.getUserId())))
                .collect(Collectors.toList()));
        respVO.setChangeLogs(userPosts.stream()
                .map(item -> buildChangeLog(item, userMap.get(item.getUserId())))
                .collect(Collectors.toList()));
        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUsers(PostLevelAssignUsersReqVO reqVO) {
        validatePost(reqVO.getPostId());
        List<PostLevelAssignUserItemReqVO> assignments = deduplicateAssignments(reqVO.getAssignments());
        validateUsers(assignments.stream().map(PostLevelAssignUserItemReqVO::getUserId).collect(Collectors.toSet()));

        List<UserPostDO> oldRelations = userPostMapper.selectListByPostId(reqVO.getPostId());
        Set<Long> impactedUserIds = new HashSet<>(oldRelations.stream().map(UserPostDO::getUserId).collect(Collectors.toSet()));
        impactedUserIds.addAll(assignments.stream().map(PostLevelAssignUserItemReqVO::getUserId).collect(Collectors.toSet()));

        userPostMapper.deleteByPostId(reqVO.getPostId());
        if (CollUtil.isNotEmpty(assignments)) {
            userPostMapper.insertBatch(assignments.stream()
                    .map(item -> buildUserPostDO(reqVO.getPostId(), item))
                    .collect(Collectors.toList()));
        }

        impactedUserIds.forEach(this::refreshUserPostIds);
    }

    private ScopeData loadScopeData(Long deptId) {
        List<DeptDO> allDepts = deptMapper.selectList(new DeptListReqVO());
        Map<Long, DeptDO> deptMap = allDepts.stream().collect(Collectors.toMap(DeptDO::getId, dept -> dept));
        Map<Long, List<DeptDO>> childDeptMap = allDepts.stream().collect(Collectors.groupingBy(DeptDO::getParentId));

        Set<Long> deptIds = new HashSet<>();
        if (deptId != null) {
            deptIds.add(deptId);
            collectChildDeptIds(deptId, childDeptMap, deptIds);
        } else {
            deptIds.addAll(allDepts.stream().map(DeptDO::getId).collect(Collectors.toSet()));
        }

        List<DeptDO> scopeDepts = allDepts.stream()
                .filter(dept -> deptIds.contains(dept.getId()))
                .sorted(Comparator.comparing(DeptDO::getSort, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(DeptDO::getId))
                .collect(Collectors.toList());
        List<PostDO> posts = deptIds.isEmpty() ? Collections.emptyList() : postMapper.selectListByDeptIds(deptIds, null);
        List<UserPostDO> userPosts = posts.isEmpty()
                ? Collections.emptyList()
                : sortUserPosts(userPostMapper.selectListByPostIds(posts.stream()
                        .map(PostDO::getId)
                        .collect(Collectors.toSet())));
        Map<Long, List<UserPostDO>> userPostsByPostId = userPosts.stream()
                .collect(Collectors.groupingBy(UserPostDO::getPostId, LinkedHashMap::new, Collectors.toList()));
        return new ScopeData(scopeDepts, deptMap, deptIds, posts, userPosts, userPostsByPostId);
    }

    private List<PostLevelSummaryRespVO> buildLevelSummaries(List<PostDO> posts, Map<Long, List<UserPostDO>> userPostsByPostId) {
        Map<String, List<PostDO>> groupedPosts = posts.stream()
                .collect(Collectors.groupingBy(this::resolveLevel, LinkedHashMap::new, Collectors.toList()));
        List<PostLevelSummaryRespVO> result = new ArrayList<>();
        for (String level : orderedLevels(groupedPosts.keySet())) {
            List<PostDO> levelPosts = groupedPosts.getOrDefault(level, Collections.emptyList());
            PostLevelSummaryRespVO summary = new PostLevelSummaryRespVO();
            summary.setLevel(level);
            summary.setPostCount(levelPosts.size());
            int assigned = levelPosts.stream()
                    .mapToInt(post -> countAssignedUser(userPostsByPostId.get(post.getId())))
                    .sum();
            int quota = levelPosts.stream().mapToInt(post -> safeInt(post.getStaffQuota())).sum();
            summary.setAssignedUserCount(assigned);
            summary.setVacancyCount(Math.max(quota - assigned, 0));
            result.add(summary);
        }
        return result;
    }

    private Map<Long, DeptAggregate> buildDeptAggregateMap(List<DeptDO> depts, List<PostDO> posts,
                                                           Map<Long, List<UserPostDO>> userPostsByPostId) {
        Map<Long, DeptAggregate> aggregateMap = new HashMap<>();
        Map<Long, List<PostDO>> deptPostsMap = posts.stream().collect(Collectors.groupingBy(PostDO::getDeptId));
        Map<Long, List<DeptDO>> childDeptMap = depts.stream().collect(Collectors.groupingBy(DeptDO::getParentId));
        List<DeptDO> orderedDepts = new ArrayList<>(depts);
        orderedDepts.sort(Comparator.comparing(DeptDO::getParentId, Comparator.nullsLast(Long::compareTo))
                .thenComparing(DeptDO::getSort, Comparator.nullsLast(Integer::compareTo))
                .reversed());

        for (DeptDO dept : orderedDepts) {
            DeptAggregate aggregate = new DeptAggregate();
            List<PostDO> directPosts = deptPostsMap.getOrDefault(dept.getId(), Collections.emptyList());
            aggregate.childDeptCount = childDeptMap.getOrDefault(dept.getId(), Collections.emptyList()).size();
            aggregate.directPostCount = directPosts.size();
            aggregate.postCount = directPosts.size();
            aggregate.staffQuota = directPosts.stream().mapToInt(post -> safeInt(post.getStaffQuota())).sum();
            aggregate.assignedUserCount = directPosts.stream()
                    .mapToInt(post -> countAssignedUser(userPostsByPostId.get(post.getId())))
                    .sum();
            for (DeptDO childDept : childDeptMap.getOrDefault(dept.getId(), Collections.emptyList())) {
                DeptAggregate childAggregate = aggregateMap.get(childDept.getId());
                if (childAggregate == null) {
                    continue;
                }
                aggregate.postCount += childAggregate.postCount;
                aggregate.staffQuota += childAggregate.staffQuota;
                aggregate.assignedUserCount += childAggregate.assignedUserCount;
            }
            aggregateMap.put(dept.getId(), aggregate);
        }
        return aggregateMap;
    }

    private PostOrgTreeNodeRespVO buildDeptOrgNode(DeptDO dept, int depth,
                                                   Map<Long, List<DeptDO>> childDeptMap,
                                                   Map<Long, List<PostDO>> deptPostsMap,
                                                   Map<Long, DeptAggregate> aggregateMap,
                                                   Map<Long, List<UserPostDO>> userPostsByPostId,
                                                   Map<Long, DeptDO> deptMap,
                                                   Set<Long> scopeDeptIds) {
        DeptAggregate aggregate = aggregateMap.getOrDefault(dept.getId(), new DeptAggregate());
        PostOrgTreeNodeRespVO node = new PostOrgTreeNodeRespVO();
        node.setId("dept-" + dept.getId());
        node.setType(isRootDept(dept, scopeDeptIds) ? "root" : "dept");
        node.setDeptId(dept.getId());
        node.setParentId(dept.getParentId());
        node.setLabel(dept.getName());
        node.setFullPath(buildDeptPath(dept, deptMap, scopeDeptIds));
        node.setSort(dept.getSort());
        node.setDepth(depth);
        node.setChildDeptCount(aggregate.childDeptCount);
        node.setDirectPostCount(aggregate.directPostCount);
        node.setPostCount(aggregate.postCount);
        node.setStaffQuota(aggregate.staffQuota);
        node.setAssignedUserCount(aggregate.assignedUserCount);
        node.setStatus(dept.getStatus());

        List<PostOrgTreeNodeRespVO> children = new ArrayList<>();
        childDeptMap.getOrDefault(dept.getId(), Collections.emptyList()).stream()
                .sorted(Comparator.comparing(DeptDO::getSort, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(DeptDO::getId))
                .map(child -> buildDeptOrgNode(child, depth + 1, childDeptMap, deptPostsMap, aggregateMap, userPostsByPostId,
                        deptMap, scopeDeptIds))
                .forEach(children::add);
        deptPostsMap.getOrDefault(dept.getId(), Collections.emptyList()).stream()
                .sorted(Comparator.comparing(PostDO::getSort, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(PostDO::getId))
                .map(post -> buildPostOrgNode(post, depth + 1, userPostsByPostId.get(post.getId()), buildDeptPath(dept, deptMap, scopeDeptIds)))
                .forEach(children::add);
        node.setChildren(children);
        return node;
    }

    private PostOrgTreeNodeRespVO buildPostOrgNode(PostDO post, int depth, List<UserPostDO> userPosts, String deptPath) {
        PostOrgTreeNodeRespVO node = new PostOrgTreeNodeRespVO();
        node.setId("post-" + post.getId());
        node.setType("post");
        node.setDeptId(post.getDeptId());
        node.setPostId(post.getId());
        node.setParentId(post.getDeptId());
        node.setLabel(post.getName());
        node.setFullPath(StrUtil.blankToDefault(deptPath, ROOT_LABEL) + " / " + post.getName());
        node.setSort(post.getSort());
        node.setDepth(depth);
        node.setChildDeptCount(0);
        node.setDirectPostCount(0);
        node.setPostCount(1);
        node.setStaffQuota(safeInt(post.getStaffQuota()));
        node.setAssignedUserCount(countAssignedUser(userPosts));
        node.setStatus(post.getStatus());
        node.setLevel(resolveLevel(post));
        node.setChildren(Collections.emptyList());
        return node;
    }

    private PostOrgTreeNodeRespVO pruneEmptyOrgNode(PostOrgTreeNodeRespVO node) {
        if (node == null) {
            return null;
        }
        if ("post".equals(node.getType())) {
            return node;
        }
        List<PostOrgTreeNodeRespVO> visibleChildren = node.getChildren() == null
                ? Collections.emptyList()
                : node.getChildren().stream()
                .map(this::pruneEmptyOrgNode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        int directDeptCount = (int) visibleChildren.stream()
                .filter(child -> "dept".equals(child.getType()) || "root".equals(child.getType()))
                .count();
        int directPostCount = (int) visibleChildren.stream()
                .filter(child -> "post".equals(child.getType()))
                .count();
        int postCount = visibleChildren.stream()
                .mapToInt(child -> safeInt(child.getPostCount()))
                .sum();
        if (postCount <= 0) {
            return null;
        }
        node.setChildren(visibleChildren);
        node.setChildDeptCount(directDeptCount);
        node.setDirectPostCount(directPostCount);
        node.setPostCount(postCount);
        node.setStaffQuota(visibleChildren.stream().mapToInt(child -> safeInt(child.getStaffQuota())).sum());
        node.setAssignedUserCount(visibleChildren.stream().mapToInt(child -> safeInt(child.getAssignedUserCount())).sum());
        return node;
    }

    private PostLevelTreeNodeRespVO buildPostLevelNode(PostDO post, List<UserPostDO> userPosts,
                                                       Map<Long, AdminUserDO> userMap) {
        PostLevelTreeNodeRespVO node = new PostLevelTreeNodeRespVO();
        node.setId("post-" + post.getId());
        node.setPostId(post.getId());
        node.setLabel(post.getName());
        node.setType("post");
        node.setLevel(resolveLevel(post));
        node.setDeptId(post.getDeptId());
        node.setAssignedUserCount(countAssignedUser(userPosts));
        node.setStaffQuota(safeInt(post.getStaffQuota()));
        node.setVacancyCount(Math.max(safeInt(post.getStaffQuota()) - safeInt(node.getAssignedUserCount()), 0));
        node.setPrimaryUserName(resolvePrimaryUserName(userPosts, userMap));
        node.setPreviewUserNames(resolvePreviewUserNames(userPosts, userMap));
        node.setChildren(Collections.emptyList());
        return node;
    }

    private String resolvePrimaryUserName(List<UserPostDO> userPosts, Map<Long, AdminUserDO> userMap) {
        if (CollUtil.isEmpty(userPosts)) {
            return null;
        }
        return userPosts.stream()
                .filter(item -> Boolean.TRUE.equals(item.getIsPrimary()))
                .map(item -> resolveUserDisplayName(userMap.get(item.getUserId())))
                .filter(StrUtil::isNotBlank)
                .findFirst()
                .orElseGet(() -> userPosts.stream()
                        .map(item -> resolveUserDisplayName(userMap.get(item.getUserId())))
                        .filter(StrUtil::isNotBlank)
                        .findFirst()
                        .orElse(null));
    }

    private List<String> resolvePreviewUserNames(List<UserPostDO> userPosts, Map<Long, AdminUserDO> userMap) {
        if (CollUtil.isEmpty(userPosts)) {
            return Collections.emptyList();
        }
        return userPosts.stream()
                .map(item -> resolveUserDisplayName(userMap.get(item.getUserId())))
                .filter(StrUtil::isNotBlank)
                .limit(3)
                .collect(Collectors.toList());
    }

    private String resolveUserDisplayName(AdminUserDO user) {
        if (user == null) {
            return null;
        }
        return StrUtil.blankToDefault(user.getNickname(), user.getUsername());
    }

    private PostLevelAssignedUserRespVO buildAssignedUser(UserPostDO userPost, AdminUserDO user) {
        PostLevelAssignedUserRespVO respVO = new PostLevelAssignedUserRespVO();
        respVO.setUserId(userPost.getUserId());
        respVO.setUsername(user == null ? null : user.getUsername());
        respVO.setNickname(user == null ? null : user.getNickname());
        respVO.setMobile(user == null ? null : user.getMobile());
        respVO.setStatus(user == null ? CommonStatusEnum.DISABLE.getStatus() : user.getStatus());
        respVO.setPrimary(Boolean.TRUE.equals(userPost.getIsPrimary()));
        respVO.setStartDate(userPost.getStartDate());
        respVO.setEndDate(userPost.getEndDate());
        respVO.setRemark(userPost.getRemark());
        return respVO;
    }

    private PostLevelChangeLogRespVO buildChangeLog(UserPostDO userPost, AdminUserDO user) {
        PostLevelChangeLogRespVO respVO = new PostLevelChangeLogRespVO();
        respVO.setUserId(userPost.getUserId());
        respVO.setNickname(user == null ? null : user.getNickname());
        respVO.setAction(Boolean.TRUE.equals(userPost.getIsPrimary()) ? "\u4e3b\u5c97\u4efb\u804c" : "\u517c\u5c97\u4efb\u804c");
        respVO.setActionTime(ObjUtil.defaultIfNull(userPost.getStartDate(), userPost.getCreateTime()));
        respVO.setRemark(userPost.getRemark());
        return respVO;
    }

    private UserPostDO buildUserPostDO(Long postId, PostLevelAssignUserItemReqVO item) {
        UserPostDO userPostDO = new UserPostDO();
        userPostDO.setPostId(postId);
        userPostDO.setUserId(item.getUserId());
        userPostDO.setIsPrimary(Boolean.TRUE.equals(item.getPrimary()));
        userPostDO.setStartDate(item.getStartDate());
        userPostDO.setEndDate(item.getEndDate());
        userPostDO.setRemark(item.getRemark());
        return userPostDO;
    }

    private void refreshUserPostIds(Long userId) {
        AdminUserDO user = adminUserMapper.selectById(userId);
        if (user == null) {
            return;
        }
        Set<Long> postIds = userPostMapper.selectListByUserId(userId).stream()
                .map(UserPostDO::getPostId)
                .collect(Collectors.toCollection(HashSet::new));
        AdminUserDO updateObj = new AdminUserDO();
        updateObj.setId(userId);
        updateObj.setPostIds(postIds);
        adminUserMapper.updateById(updateObj);
    }

    private void validatePost(Long postId) {
        PostDO post = postMapper.selectById(postId);
        if (post == null) {
            throw exception(POST_NOT_FOUND);
        }
        if (!Objects.equals(post.getStatus(), CommonStatusEnum.ENABLE.getStatus())) {
            throw exception(POST_NOT_ENABLE, post.getName());
        }
    }

    private void validateUsers(Set<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return;
        }
        Map<Long, AdminUserDO> userMap = getUserMap(userIds);
        for (Long userId : userIds) {
            AdminUserDO user = userMap.get(userId);
            if (user == null) {
                throw exception(USER_NOT_EXISTS);
            }
            if (!Objects.equals(user.getStatus(), CommonStatusEnum.ENABLE.getStatus())) {
                throw exception(USER_IS_DISABLE, user.getNickname());
            }
        }
    }

    private Map<Long, AdminUserDO> getUserMap(Set<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        return adminUserMapper.selectByIds(userIds).stream()
                .collect(Collectors.toMap(AdminUserDO::getId, user -> user));
    }

    private void collectChildDeptIds(Long deptId, Map<Long, List<DeptDO>> childDeptMap, Set<Long> result) {
        for (DeptDO childDept : childDeptMap.getOrDefault(deptId, Collections.emptyList())) {
            if (result.add(childDept.getId())) {
                collectChildDeptIds(childDept.getId(), childDeptMap, result);
            }
        }
    }

    private List<PostLevelAssignUserItemReqVO> deduplicateAssignments(List<PostLevelAssignUserItemReqVO> assignments) {
        if (CollUtil.isEmpty(assignments)) {
            return Collections.emptyList();
        }
        Map<Long, PostLevelAssignUserItemReqVO> deduplicated = new LinkedHashMap<>();
        assignments.stream()
                .filter(item -> item != null && item.getUserId() != null)
                .forEach(item -> deduplicated.put(item.getUserId(), item));
        return new ArrayList<>(deduplicated.values());
    }

    private List<UserPostDO> sortUserPosts(List<UserPostDO> userPosts) {
        if (CollUtil.isEmpty(userPosts)) {
            return Collections.emptyList();
        }
        return userPosts.stream()
                .sorted(Comparator.comparing(UserPostDO::getIsPrimary, Comparator.nullsLast(Boolean::compareTo)).reversed()
                        .thenComparing(UserPostDO::getStartDate, Comparator.nullsLast(LocalDateTime::compareTo))
                        .thenComparing(UserPostDO::getUserId))
                .collect(Collectors.toList());
    }

    private List<String> orderedLevels(Collection<String> levels) {
        List<String> ordered = new ArrayList<>();
        for (String candidate : LEVEL_ORDER) {
            if (levels.contains(candidate)) {
                ordered.add(candidate);
            }
        }
        levels.stream()
                .filter(level -> !ordered.contains(level))
                .sorted()
                .forEach(ordered::add);
        return ordered;
    }

    private String resolveScopeLabel(ScopeData scopeData, Long deptId) {
        if (deptId == null) {
            return ROOT_LABEL;
        }
        DeptDO dept = scopeData.deptMap.get(deptId);
        return dept == null ? ROOT_LABEL : dept.getName();
    }

    private String buildDeptPath(DeptDO dept, Map<Long, DeptDO> deptMap, Set<Long> scopeDeptIds) {
        List<String> path = new ArrayList<>();
        DeptDO current = dept;
        int guard = 0;
        while (current != null && guard++ < 32) {
            path.add(current.getName());
            if (Objects.equals(current.getParentId(), DeptDO.PARENT_ID_ROOT)) {
                break;
            }
            current = scopeDeptIds.contains(current.getParentId()) ? deptMap.get(current.getParentId()) : null;
        }
        Collections.reverse(path);
        return String.join(" / ", path);
    }

    private boolean isRootDept(DeptDO dept, Set<Long> scopeDeptIds) {
        return Objects.equals(dept.getParentId(), DeptDO.PARENT_ID_ROOT) || !scopeDeptIds.contains(dept.getParentId());
    }

    private int calculateDeptDepth(List<PostOrgTreeNodeRespVO> nodes) {
        return nodes.stream().mapToInt(this::calculateDeptDepth).max().orElse(0);
    }

    private int calculateDeptDepth(PostOrgTreeNodeRespVO node) {
        int currentDepth = ObjUtil.defaultIfNull(node.getDepth(), 0);
        int childDepth = node.getChildren() == null ? 0 : node.getChildren().stream()
                .filter(child -> !"post".equals(child.getType()))
                .mapToInt(this::calculateDeptDepth)
                .max()
                .orElse(0);
        return Math.max(currentDepth, childDepth);
    }

    private int countAssignedUser(Collection<UserPostDO> userPosts) {
        return CollUtil.isEmpty(userPosts) ? 0 : userPosts.size();
    }

    private int countVisibleDeptNodes(List<PostOrgTreeNodeRespVO> nodes) {
        return nodes.stream().mapToInt(this::countVisibleDeptNodes).sum();
    }

    private int countVisibleDeptNodes(PostOrgTreeNodeRespVO node) {
        if (node == null || "post".equals(node.getType())) {
            return 0;
        }
        return 1 + (node.getChildren() == null ? 0 : node.getChildren().stream()
                .mapToInt(this::countVisibleDeptNodes)
                .sum());
    }

    private int countVisiblePostNodes(List<PostOrgTreeNodeRespVO> nodes) {
        return nodes.stream().mapToInt(this::countVisiblePostNodes).sum();
    }

    private int countVisiblePostNodes(PostOrgTreeNodeRespVO node) {
        if (node == null) {
            return 0;
        }
        if ("post".equals(node.getType())) {
            return 1;
        }
        return node.getChildren() == null ? 0 : node.getChildren().stream()
                .mapToInt(this::countVisiblePostNodes)
                .sum();
    }

    private int sumVisibleStaffQuota(List<PostOrgTreeNodeRespVO> nodes) {
        return nodes.stream().mapToInt(node -> safeInt(node.getStaffQuota())).sum();
    }

    private int sumVisibleAssignedUsers(List<PostOrgTreeNodeRespVO> nodes) {
        return nodes.stream().mapToInt(node -> safeInt(node.getAssignedUserCount())).sum();
    }

    private int countAssignedUserTotal(Map<Long, List<UserPostDO>> userPostsByPostId) {
        return userPostsByPostId.values().stream().mapToInt(this::countAssignedUser).sum();
    }

    private int countPartTimeUsers(List<UserPostDO> userPosts) {
        if (CollUtil.isEmpty(userPosts)) {
            return 0;
        }
        return (int) userPosts.stream()
                .collect(Collectors.groupingBy(UserPostDO::getUserId, Collectors.counting()))
                .values().stream()
                .filter(count -> count > 1)
                .count();
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String resolveLevel(PostDO post) {
        return PostLevelClassifier.resolveLevel(post == null ? null : post.getLevel(),
                post == null ? null : post.getName());
    }

    private String buildLevelNodeId(Long deptId, String level) {
        return deptId == null ? "level-" + level : "level-" + deptId + "-" + level;
    }

    private static class ScopeData {
        private final List<DeptDO> depts;
        private final Map<Long, DeptDO> deptMap;
        private final Set<Long> deptIds;
        private final List<PostDO> posts;
        private final List<UserPostDO> userPosts;
        private final Map<Long, List<UserPostDO>> userPostsByPostId;

        private ScopeData(List<DeptDO> depts, Map<Long, DeptDO> deptMap, Set<Long> deptIds, List<PostDO> posts,
                          List<UserPostDO> userPosts, Map<Long, List<UserPostDO>> userPostsByPostId) {
            this.depts = depts;
            this.deptMap = deptMap;
            this.deptIds = deptIds;
            this.posts = posts;
            this.userPosts = userPosts;
            this.userPostsByPostId = userPostsByPostId;
        }
    }

    private static class DeptAggregate {
        private int childDeptCount;
        private int directPostCount;
        private int postCount;
        private int staffQuota;
        private int assignedUserCount;
    }

}
