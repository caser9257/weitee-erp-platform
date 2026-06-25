package cn.weitee.erp.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.module.system.dal.dataobject.permission.RoleDO;
import cn.weitee.erp.module.system.enums.permission.RoleCodeEnum;
import cn.weitee.erp.module.system.service.permission.PermissionService;
import cn.weitee.erp.module.system.service.permission.RoleService;
import cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 财务数据权限服务实现
 * 基于 erp_finance_ledger_role 表实现账簿级数据权限控制
 */
@Slf4j
@Service
public class FinanceDataPermissionServiceImpl implements FinanceDataPermissionService {

    /**
     * 审计角色标识
     */
    private static final String AUDIT_ROLE_CODE = "finance_audit";

    @Resource
    private ErpFinanceLedgerRoleService ledgerRoleService;
    @Resource
    private PermissionService permissionService;
    @Resource
    private RoleService roleService;

    @Override
    public List<Long> getVisibleLedgerIds() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return Collections.emptyList();
        }
        return getVisibleLedgerIds(userId);
    }

    @Override
    public List<Long> getVisibleLedgerIds(Long userId) {
        // 获取用户的角色ID列表
        List<Long> roleIds = getUserRoleIds(userId);
        if (CollUtil.isEmpty(roleIds)) {
            return Collections.emptyList();
        }

        // 如果是管理员角色，返回null表示不限制
        if (isAdminRole(roleIds)) {
            return null;
        }

        // 查询角色关联的账簿ID列表
        List<Long> ledgerIds = ledgerRoleService.getVisibleLedgerIdsByRoleIds(roleIds);
        if (CollUtil.isEmpty(ledgerIds)) {
            // 如果没有配置账簿权限，默认返回空（不可见任何账簿）
            // 注意：如果需要"未配置时默认可见所有"的逻辑，这里应该返回null
            return Collections.emptyList();
        }
        return ledgerIds;
    }

    @Override
    public boolean canAccessLedger(Long ledgerId) {
        List<Long> visibleLedgerIds = getVisibleLedgerIds();
        // null表示不限制
        if (visibleLedgerIds == null) {
            return true;
        }
        return visibleLedgerIds.contains(ledgerId);
    }

    @Override
    public boolean isAuditRole() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return false;
        }
        return permissionService.hasAnyRoles(userId, AUDIT_ROLE_CODE);
    }

    @Override
    public List<Long> getVisibleDeptIds() {
        // TODO: 实现部门级数据权限
        return null;
    }

    @Override
    public List<String> getVisibleSubjectCodes() {
        // TODO: 实现科目级数据权限
        return null;
    }

    /**
     * 获取用户的角色ID列表
     */
    private List<Long> getUserRoleIds(Long userId) {
        Set<Long> roleIds = permissionService.getUserRoleIdListByUserIdFromCache(userId);
        if (CollUtil.isEmpty(roleIds)) {
            return Collections.emptyList();
        }
        return new ArrayList<>(roleIds);
    }

    /**
     * 检查是否为管理员角色
     */
    private boolean isAdminRole(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return false;
        }
        List<RoleDO> roles = roleService.getRoleListFromCache(roleIds);
        return roles.stream().anyMatch(role -> role != null
                && RoleCodeEnum.isSuperAdmin(role.getCode()));
    }

}
