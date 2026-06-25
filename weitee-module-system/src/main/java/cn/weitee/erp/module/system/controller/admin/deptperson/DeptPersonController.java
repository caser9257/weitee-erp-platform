package cn.weitee.erp.module.system.controller.admin.deptperson;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.module.system.controller.admin.dept.vo.dept.DeptListReqVO;
import cn.weitee.erp.module.system.controller.admin.deptperson.vo.DeptPersonTreeNodeRespVO;
import cn.weitee.erp.module.system.controller.admin.deptperson.vo.DeptPersonTreeRespVO;
import cn.weitee.erp.module.system.dal.dataobject.dept.DeptDO;
import cn.weitee.erp.module.system.dal.dataobject.user.AdminUserDO;
import cn.weitee.erp.module.system.service.dept.DeptService;
import cn.weitee.erp.module.system.service.user.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin - Dept Person Tree")
@RestController
@RequestMapping("/system/dept")
@Validated
public class DeptPersonController {

    private static final String ROOT_LABEL = "\u516C\u53F8";
    private static final String TYPE_ROOT = "root";
    private static final String TYPE_DEPT = "dept";
    private static final String TYPE_BUCKET = "bucket";
    private static final String TYPE_USER = "user";

    @Resource
    private DeptService deptService;
    @Resource
    private AdminUserService adminUserService;

    @GetMapping("/person-tree")
    @Operation(summary = "Get dept person tree")
    @PreAuthorize("@ss.hasPermission('system:dept:query')")
    public CommonResult<DeptPersonTreeRespVO> getDeptPersonTree(@RequestParam(value = "deptId", required = false) Long deptId) {
        List<DeptDO> enabledDepts = deptService.getDeptList(new DeptListReqVO().setStatus(CommonStatusEnum.ENABLE.getStatus()));
        List<AdminUserDO> enabledUsers = adminUserService.getUserListByStatus(CommonStatusEnum.ENABLE.getStatus());

        DeptPersonTreeRespVO respVO = new DeptPersonTreeRespVO();
        if (CollUtil.isEmpty(enabledDepts)) {
            respVO.setTree(Collections.emptyList());
            respVO.setDeptCount(0);
            respVO.setUserCount(0);
            respVO.setLeafDeptCount(0);
            respVO.setUnassignedUserCount(0);
            respVO.setMaxDepth(0);
            return success(respVO);
        }

        Map<Long, DeptDO> deptMap = enabledDepts.stream()
                .collect(Collectors.toMap(DeptDO::getId, dept -> dept, (left, right) -> left, LinkedHashMap::new));
        Set<Long> scopeDeptIds = resolveScopeDeptIds(deptId, deptMap);
        if (CollUtil.isEmpty(scopeDeptIds)) {
            respVO.setTree(Collections.emptyList());
            respVO.setDeptCount(0);
            respVO.setUserCount(0);
            respVO.setLeafDeptCount(0);
            respVO.setUnassignedUserCount(0);
            respVO.setMaxDepth(0);
            return success(respVO);
        }

        List<DeptDO> scopeDepts = enabledDepts.stream()
                .filter(dept -> scopeDeptIds.contains(dept.getId()))
                .sorted(Comparator.comparing(DeptDO::getSort, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(DeptDO::getId))
                .collect(Collectors.toList());
        Map<Long, List<DeptDO>> childDeptMap = scopeDepts.stream()
                .collect(Collectors.groupingBy(DeptDO::getParentId, LinkedHashMap::new, Collectors.toList()));

        List<AdminUserDO> scopeUsers = enabledUsers.stream()
                .filter(user -> user.getDeptId() != null && scopeDeptIds.contains(user.getDeptId()))
                .sorted(Comparator.comparing(AdminUserDO::getNickname, Comparator.nullsLast(String::compareTo))
                        .thenComparing(AdminUserDO::getUsername, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
        Map<Long, List<AdminUserDO>> userMapByDeptId = scopeUsers.stream()
                .collect(Collectors.groupingBy(AdminUserDO::getDeptId, LinkedHashMap::new, Collectors.toList()));

        List<DeptPersonTreeNodeRespVO> tree = buildTree(deptId, deptMap, childDeptMap, userMapByDeptId, enabledUsers);
        respVO.setTree(tree);
        respVO.setDeptCount(countDept(tree));
        respVO.setUserCount(countUser(tree));
        respVO.setLeafDeptCount(countLeafDept(tree));
        respVO.setUnassignedUserCount((int) enabledUsers.stream().filter(user -> user.getDeptId() == null).count());
        respVO.setMaxDepth(countMaxDepth(tree));
        return success(respVO);
    }

    private List<DeptPersonTreeNodeRespVO> buildTree(Long deptId, Map<Long, DeptDO> deptMap,
                                                     Map<Long, List<DeptDO>> childDeptMap,
                                                     Map<Long, List<AdminUserDO>> userMapByDeptId,
                                                     List<AdminUserDO> allUsers) {
        if (deptId != null) {
            DeptDO rootDept = deptMap.get(deptId);
            if (rootDept == null) {
                return Collections.emptyList();
            }
            return Collections.singletonList(buildDeptNode(rootDept, deptMap, childDeptMap, userMapByDeptId,
                    1, buildDeptPath(deptMap, rootDept.getId())));
        }

        DeptPersonTreeNodeRespVO rootNode = new DeptPersonTreeNodeRespVO();
        rootNode.setId("root");
        rootNode.setType(TYPE_ROOT);
        rootNode.setLabel(ROOT_LABEL);
        rootNode.setFullPath(ROOT_LABEL);
        rootNode.setDepth(1);

        List<DeptPersonTreeNodeRespVO> children = new ArrayList<>();
        List<DeptDO> topDepts = childDeptMap.getOrDefault(DeptDO.PARENT_ID_ROOT, Collections.emptyList());
        for (DeptDO dept : topDepts) {
            children.add(buildDeptNode(dept, deptMap, childDeptMap, userMapByDeptId, 2, ROOT_LABEL + " / " + dept.getName()));
        }

        List<AdminUserDO> unassignedUsers = allUsers.stream()
                .filter(user -> user.getDeptId() == null)
                .sorted(Comparator.comparing(AdminUserDO::getNickname, Comparator.nullsLast(String::compareTo))
                        .thenComparing(AdminUserDO::getUsername, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(unassignedUsers)) {
            children.add(buildUserBucket(unassignedUsers, ROOT_LABEL + " / \u672A\u5206\u914D\u4EBA\u5458", 2));
        }

        rootNode.setChildren(children);
        rootNode.setChildDeptCount((int) children.stream().filter(node -> TYPE_DEPT.equals(node.getType())).count());
        rootNode.setDirectUserCount(unassignedUsers.size());
        rootNode.setDeptCount(children.stream().mapToInt(DeptPersonTreeNodeRespVO::getDeptCount).sum());
        rootNode.setUserCount(children.stream().mapToInt(DeptPersonTreeNodeRespVO::getUserCount).sum());
        rootNode.setLeafDeptCount(children.stream().mapToInt(DeptPersonTreeNodeRespVO::getLeafDeptCount).sum());
        rootNode.setMaxDepth(children.stream().mapToInt(DeptPersonTreeNodeRespVO::getDepth).max().orElse(1));
        return Collections.singletonList(rootNode);
    }

    private DeptPersonTreeNodeRespVO buildDeptNode(DeptDO dept, Map<Long, DeptDO> deptMap,
                                                   Map<Long, List<DeptDO>> childDeptMap,
                                                   Map<Long, List<AdminUserDO>> userMapByDeptId,
                                                   int depth, String fullPath) {
        List<DeptDO> childDepts = childDeptMap.getOrDefault(dept.getId(), Collections.emptyList());
        List<DeptPersonTreeNodeRespVO> children = new ArrayList<>();
        for (DeptDO childDept : childDepts) {
            children.add(buildDeptNode(childDept, deptMap, childDeptMap, userMapByDeptId, depth + 1,
                    fullPath + " / " + childDept.getName()));
        }
        List<AdminUserDO> directUsers = userMapByDeptId.getOrDefault(dept.getId(), Collections.emptyList());
        for (AdminUserDO user : directUsers) {
            children.add(buildUserNode(user, dept.getId(), fullPath, depth + 1));
        }

        DeptPersonTreeNodeRespVO node = new DeptPersonTreeNodeRespVO();
        node.setId("dept-" + dept.getId());
        node.setType(TYPE_DEPT);
        node.setDeptId(dept.getId());
        node.setParentId(dept.getParentId());
        node.setLabel(dept.getName());
        node.setFullPath(fullPath);
        node.setSort(dept.getSort());
        node.setStatus(dept.getStatus());
        node.setDepth(depth);
        node.setChildren(children);
        node.setChildDeptCount(childDepts.size());
        node.setDirectUserCount(directUsers.size());
        node.setDeptCount(1 + children.stream().mapToInt(DeptPersonTreeNodeRespVO::getDeptCount).sum());
        node.setUserCount(directUsers.size() + children.stream().mapToInt(DeptPersonTreeNodeRespVO::getUserCount).sum());
        node.setLeafDeptCount(childDepts.isEmpty()
                ? 1
                : children.stream().mapToInt(DeptPersonTreeNodeRespVO::getLeafDeptCount).sum());
        node.setMaxDepth(children.stream().mapToInt(DeptPersonTreeNodeRespVO::getDepth).max().orElse(depth));
        return node;
    }

    private DeptPersonTreeNodeRespVO buildUserBucket(List<AdminUserDO> users, String fullPath, int depth) {
        DeptPersonTreeNodeRespVO node = new DeptPersonTreeNodeRespVO();
        node.setId("bucket-unassigned");
        node.setType(TYPE_BUCKET);
        node.setLabel("\u672A\u5206\u914D\u4EBA\u5458 (" + users.size() + ")");
        node.setFullPath(fullPath);
        node.setDepth(depth);
        node.setDirectUserCount(users.size());
        node.setUserCount(users.size());
        node.setDeptCount(0);
        node.setLeafDeptCount(0);
        node.setChildren(users.stream()
                .map(user -> buildUserNode(user, null, fullPath, depth + 1))
                .collect(Collectors.toList()));
        node.setMaxDepth(node.getChildren().stream().mapToInt(DeptPersonTreeNodeRespVO::getDepth).max().orElse(depth));
        return node;
    }

    private DeptPersonTreeNodeRespVO buildUserNode(AdminUserDO user, Long parentDeptId, String parentFullPath, int depth) {
        DeptPersonTreeNodeRespVO node = new DeptPersonTreeNodeRespVO();
        node.setId("user-" + user.getId());
        node.setType(TYPE_USER);
        node.setUserId(user.getId());
        node.setParentId(parentDeptId);
        node.setLabel(buildUserLabel(user));
        node.setFullPath(parentFullPath + " / " + buildUserLabel(user));
        node.setUsername(user.getUsername());
        node.setNickname(user.getNickname());
        node.setMobile(user.getMobile());
        node.setStatus(user.getStatus());
        node.setDepth(depth);
        node.setDeptCount(0);
        node.setUserCount(1);
        node.setLeafDeptCount(0);
        node.setDirectUserCount(0);
        node.setChildren(Collections.emptyList());
        node.setMaxDepth(depth);
        return node;
    }

    private String buildUserLabel(AdminUserDO user) {
        String nickname = Objects.toString(user.getNickname(), "");
        String username = Objects.toString(user.getUsername(), "");
        if (nickname.isBlank()) {
            return username;
        }
        if (username.isBlank()) {
            return nickname;
        }
        return nickname + " (" + username + ")";
    }

    private String buildDeptPath(Map<Long, DeptDO> deptMap, Long deptId) {
        DeptDO dept = deptMap.get(deptId);
        if (dept == null) {
            return "";
        }
        if (dept.getParentId() == null || Objects.equals(dept.getParentId(), DeptDO.PARENT_ID_ROOT)) {
            return ROOT_LABEL + " / " + dept.getName();
        }
        String parentPath = buildDeptPath(deptMap, dept.getParentId());
        return parentPath.isBlank() ? ROOT_LABEL + " / " + dept.getName() : parentPath + " / " + dept.getName();
    }

    private Set<Long> resolveScopeDeptIds(Long deptId, Map<Long, DeptDO> deptMap) {
        if (deptId == null) {
            return new LinkedHashSet<>(deptMap.keySet());
        }
        if (!deptMap.containsKey(deptId)) {
            return Collections.emptySet();
        }
        Set<Long> scopeDeptIds = new LinkedHashSet<>();
        scopeDeptIds.add(deptId);
        deptService.getChildDeptList(Collections.singleton(deptId)).forEach(dept -> scopeDeptIds.add(dept.getId()));
        return scopeDeptIds;
    }

    private int countDept(List<DeptPersonTreeNodeRespVO> tree) {
        return tree.stream().mapToInt(DeptPersonTreeNodeRespVO::getDeptCount).sum();
    }

    private int countUser(List<DeptPersonTreeNodeRespVO> tree) {
        return tree.stream().mapToInt(DeptPersonTreeNodeRespVO::getUserCount).sum();
    }

    private int countLeafDept(List<DeptPersonTreeNodeRespVO> tree) {
        return tree.stream().mapToInt(DeptPersonTreeNodeRespVO::getLeafDeptCount).sum();
    }

    private int countMaxDepth(List<DeptPersonTreeNodeRespVO> tree) {
        return tree.stream().mapToInt(DeptPersonTreeNodeRespVO::getMaxDepth).max().orElse(0);
    }

}