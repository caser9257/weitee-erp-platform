package cn.weitee.erp.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerConfigDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceRoleDeptDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceRoleSubjectDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceRoleDeptMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceRoleSubjectMapper;
import cn.weitee.erp.module.erp.service.finance.interceptor.FinancePermissionScope;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
    @Resource
    private ErpFinanceDualLedgerConfigService dualLedgerConfigService;
    @Resource
    private ErpFinanceRoleDeptMapper roleDeptMapper;
    @Resource
    private ErpFinanceRoleSubjectMapper roleSubjectMapper;

    @Override
    public FinancePermissionScope getPermissionScope() {
        return getPermissionScope(SecurityFrameworkUtils.getLoginUserId());
    }

    @Override
    public FinancePermissionScope getPermissionScope(Long userId) {
        if (userId == null) {
            return FinancePermissionScope.ofLedgerScope(FinancePermissionScope.Scope.none(), false);
        }
        boolean auditOnly = userId != null && isAuditRole(userId);
        List<Long> roleIds = getUserRoleIds(userId);
        boolean unrestrictedDataScope = auditOnly || isAdminRole(roleIds);
        List<Long> visibleLedgerIds = getVisibleLedgerIds(userId);
        FinancePermissionScope.Scope<Long> ledgerScope;
        if (visibleLedgerIds == null) {
            ledgerScope = FinancePermissionScope.Scope.all();
        } else if (visibleLedgerIds.isEmpty()) {
            ledgerScope = FinancePermissionScope.Scope.none();
        } else {
            ledgerScope = FinancePermissionScope.Scope.limited(new HashSet<>(visibleLedgerIds));
        }
        return new FinancePermissionScope(ledgerScope, resolveDeptScope(roleIds, unrestrictedDataScope),
                resolveSubjectScopes(roleIds, ledgerScope, unrestrictedDataScope), auditOnly, auditOnly);
    }

    @Override
    public boolean canAccessDept(Long deptId) {
        return canAccess(getPermissionScope().deptScope(), deptId);
    }

    @Override
    public boolean canAccessSubject(Long ledgerId, String subjectCode) {
        if (ledgerId == null || subjectCode == null || !canAccessLedger(ledgerId)) {
            return false;
        }
        FinancePermissionScope.Scope<String> subjectScope = getPermissionScope()
                .subjectScopesByLedger().getOrDefault(ledgerId, FinancePermissionScope.Scope.all());
        return canAccess(subjectScope, subjectCode);
    }

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
        if (isAuditRole(userId)) {
            Set<Long> internalLedgerIds = getInternalLedgerIds();
            ledgerIds = ledgerIds.stream()
                    .filter(ledgerId -> !internalLedgerIds.contains(ledgerId))
                    .distinct()
                    .collect(Collectors.toList());
        }
        return ledgerIds;
    }

    @Override
    public boolean canAccessLedger(Long ledgerId) {
        return canAccessLedger(SecurityFrameworkUtils.getLoginUserId(), ledgerId);
    }

    @Override
    public boolean canAccessLedger(Long userId, Long ledgerId) {
        if (userId == null || ledgerId == null) {
            return false;
        }
        List<Long> visibleLedgerIds = getVisibleLedgerIds(userId);
        // null表示不限制
        if (visibleLedgerIds == null) {
            return true;
        }
        return visibleLedgerIds.contains(ledgerId);
    }

    @Override
    public boolean canAccessDualLedger(Long userId, Integer bizType) {
        if (userId == null || isAuditRole(userId)) {
            return false;
        }
        List<ErpFinanceDualLedgerConfigDO> configs = bizType == null
                ? dualLedgerConfigService.getDualLedgerConfigListByStatus(CommonStatusEnum.ENABLE.getStatus())
                : Collections.singletonList(dualLedgerConfigService.getEnabledDualLedgerConfig(bizType));
        if (CollUtil.isEmpty(configs) || configs.stream().anyMatch(Objects::isNull)) {
            return false;
        }
        List<Long> visibleLedgerIds = getVisibleLedgerIds(userId);
        if (visibleLedgerIds == null) {
            return true;
        }
        Set<Long> visible = new HashSet<>(visibleLedgerIds);
        return configs.stream()
                .allMatch(config -> visible.contains(config.getExternalLedgerId())
                        && visible.contains(config.getInternalLedgerId()));
    }

    @Override
    public boolean isAuditRole() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return false;
        }
        return isAuditRole(userId);
    }

    private boolean isAuditRole(Long userId) {
        return permissionService.hasAnyRoles(userId, AUDIT_ROLE_CODE);
    }

    @Override
    public List<Long> getVisibleDeptIds() {
        FinancePermissionScope.Scope<Long> deptScope = getPermissionScope().deptScope();
        return toLegacyList(deptScope);
    }

    @Override
    public List<String> getVisibleSubjectCodes() {
        Map<Long, FinancePermissionScope.Scope<String>> subjectScopes = getPermissionScope().subjectScopesByLedger();
        if (subjectScopes.isEmpty() || subjectScopes.values().stream()
                .anyMatch(scope -> scope.mode() == FinancePermissionScope.ScopeMode.ALL)) {
            return null;
        }
        return subjectScopes.values().stream()
                .filter(scope -> scope.mode() == FinancePermissionScope.ScopeMode.LIMITED)
                .flatMap(scope -> scope.values().stream())
                .distinct()
                .collect(Collectors.toList());
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

    private Set<Long> getInternalLedgerIds() {
        List<ErpFinanceDualLedgerConfigDO> configs = dualLedgerConfigService
                .getDualLedgerConfigListByStatus(CommonStatusEnum.ENABLE.getStatus());
        if (CollUtil.isEmpty(configs)) {
            return Collections.emptySet();
        }
        return configs.stream()
                .filter(Objects::nonNull)
                .map(ErpFinanceDualLedgerConfigDO::getInternalLedgerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private <T> List<T> toLegacyList(FinancePermissionScope.Scope<T> scope) {
        if (scope.mode() == FinancePermissionScope.ScopeMode.ALL) {
            return null;
        }
        return new ArrayList<>(scope.values());
    }

    private FinancePermissionScope.Scope<Long> resolveDeptScope(List<Long> roleIds, boolean unrestrictedDataScope) {
        if (unrestrictedDataScope) {
            return FinancePermissionScope.Scope.all();
        }
        if (roleDeptMapper == null || CollUtil.isEmpty(roleIds)) {
            return FinancePermissionScope.Scope.none();
        }
        List<ErpFinanceRoleDeptDO> mappings = roleDeptMapper.selectListByRoleIds(roleIds);
        if (CollUtil.isEmpty(mappings)) {
            return FinancePermissionScope.Scope.none();
        }
        return FinancePermissionScope.Scope.limited(mappings.stream()
                .map(ErpFinanceRoleDeptDO::getDeptId).filter(Objects::nonNull).collect(Collectors.toSet()));
    }

    private Map<Long, FinancePermissionScope.Scope<String>> resolveSubjectScopes(List<Long> roleIds,
                                                                                   FinancePermissionScope.Scope<Long> ledgerScope,
                                                                                   boolean unrestrictedDataScope) {
        if (unrestrictedDataScope || ledgerScope.mode() != FinancePermissionScope.ScopeMode.LIMITED) {
            return Collections.emptyMap();
        }
        Map<Long, FinancePermissionScope.Scope<String>> scopes = ledgerScope.values().stream()
                .collect(Collectors.toMap(ledgerId -> ledgerId, ledgerId -> FinancePermissionScope.Scope.none()));
        if (roleSubjectMapper == null || CollUtil.isEmpty(roleIds)) {
            return scopes;
        }
        List<ErpFinanceRoleSubjectDO> mappings = roleSubjectMapper.selectListByRoleIds(roleIds);
        if (CollUtil.isEmpty(mappings)) {
            return scopes;
        }
        Map<Long, FinancePermissionScope.Scope<String>> mappedScopes = mappings.stream()
                .filter(mapping -> mapping.getLedgerId() != null && mapping.getSubjectCode() != null)
                .filter(mapping -> ledgerScope.values().contains(mapping.getLedgerId()))
                .collect(Collectors.groupingBy(ErpFinanceRoleSubjectDO::getLedgerId,
                        Collectors.collectingAndThen(Collectors.mapping(ErpFinanceRoleSubjectDO::getSubjectCode,
                                Collectors.toSet()), FinancePermissionScope.Scope::limited)));
        scopes.putAll(mappedScopes);
        return scopes;
    }

    private <T> boolean canAccess(FinancePermissionScope.Scope<T> scope, T value) {
        return scope.mode() == FinancePermissionScope.ScopeMode.ALL
                || scope.mode() == FinancePermissionScope.ScopeMode.LIMITED && scope.values().contains(value);
    }

}
