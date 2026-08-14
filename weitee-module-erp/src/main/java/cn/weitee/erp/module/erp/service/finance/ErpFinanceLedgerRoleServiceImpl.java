package cn.weitee.erp.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerRoleDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceRoleDeptDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceRoleSubjectDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceLedgerRoleMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceRoleDeptMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceRoleSubjectMapper;
import cn.weitee.erp.module.system.service.permission.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * ERP 账簿角色关联 Service 实现
 */
@Slf4j
@Service
public class ErpFinanceLedgerRoleServiceImpl implements ErpFinanceLedgerRoleService {

    @Resource
    private ErpFinanceLedgerRoleMapper ledgerRoleMapper;

    @Resource
    private UserLedgerPermissionCacheService userLedgerPermissionCacheService;
    @Resource
    private PermissionService permissionService;
    @Resource
    private ErpFinanceRoleDeptMapper roleDeptMapper;
    @Resource
    private ErpFinanceRoleSubjectMapper roleSubjectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createLedgerRole(Long ledgerId, Long roleId) {
        ErpFinanceLedgerRoleDO ledgerRole = ErpFinanceLedgerRoleDO.builder()
                .ledgerId(ledgerId)
                .roleId(roleId)
                .status(CommonStatusEnum.ENABLE.getStatus())
                .build();
        ledgerRoleMapper.insert(ledgerRole);
        return ledgerRole.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLedgerRole(Long ledgerId, Long roleId) {
        ledgerRoleMapper.delete(new cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX<ErpFinanceLedgerRoleDO>()
                .eq(ErpFinanceLedgerRoleDO::getLedgerId, ledgerId)
                .eq(ErpFinanceLedgerRoleDO::getRoleId, roleId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setLedgerRoles(Long ledgerId, List<Long> roleIds) {
        List<Long> existingRoleIds = ledgerRoleMapper.selectRoleIdsByLedgerId(ledgerId);
        Set<Long> affectedRoleIds = new java.util.LinkedHashSet<>(
                existingRoleIds == null ? java.util.Collections.emptyList() : existingRoleIds);
        if (CollUtil.isNotEmpty(roleIds)) {
            affectedRoleIds.addAll(roleIds);
        }
        // 删除旧关联
        ledgerRoleMapper.delete(new cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX<ErpFinanceLedgerRoleDO>()
                .eq(ErpFinanceLedgerRoleDO::getLedgerId, ledgerId));

        // 创建新关联
        if (CollUtil.isNotEmpty(roleIds)) {
            for (Long roleId : roleIds) {
                createLedgerRole(ledgerId, roleId);
            }
        }

        clearAffectedUserCaches(new java.util.ArrayList<>(affectedRoleIds));

        // 清除相关用户的缓存（简化处理，实际应该只清除相关角色的用户缓存）
        log.info("账簿角色关联变更，需要清除相关用户缓存。ledgerId={}", ledgerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setRoleDeptIds(Long roleId, List<Long> deptIds) {
        roleDeptMapper.delete(new cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX<ErpFinanceRoleDeptDO>()
                .eq(ErpFinanceRoleDeptDO::getRoleId, roleId));
        List<Long> distinctDeptIds = deptIds == null ? List.of() : deptIds.stream().distinct().toList();
        if (CollUtil.isNotEmpty(distinctDeptIds)) {
            roleDeptMapper.insertBatch(distinctDeptIds.stream().map(deptId -> new ErpFinanceRoleDeptDO()
                    .setRoleId(roleId).setDeptId(deptId).setStatus(CommonStatusEnum.ENABLE.getStatus())).toList());
        }
        clearAffectedUserCaches(List.of(roleId));
    }

    @Override
    public List<Long> getRoleDeptIds(Long roleId) {
        return roleDeptMapper.selectListByRoleIds(List.of(roleId)).stream()
                .map(ErpFinanceRoleDeptDO::getDeptId).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setRoleSubjectCodes(Long roleId, Long ledgerId, List<String> subjectCodes) {
        roleSubjectMapper.delete(new cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX<ErpFinanceRoleSubjectDO>()
                .eq(ErpFinanceRoleSubjectDO::getRoleId, roleId)
                .eq(ErpFinanceRoleSubjectDO::getLedgerId, ledgerId));
        List<String> distinctSubjectCodes = subjectCodes == null ? List.of() : subjectCodes.stream().distinct().toList();
        if (CollUtil.isNotEmpty(distinctSubjectCodes)) {
            roleSubjectMapper.insertBatch(distinctSubjectCodes.stream().map(subjectCode -> new ErpFinanceRoleSubjectDO()
                    .setRoleId(roleId).setLedgerId(ledgerId).setSubjectCode(subjectCode)
                    .setStatus(CommonStatusEnum.ENABLE.getStatus())).toList());
        }
        clearAffectedUserCaches(List.of(roleId));
    }

    @Override
    public List<String> getRoleSubjectCodes(Long roleId, Long ledgerId) {
        return roleSubjectMapper.selectListByRoleIds(List.of(roleId)).stream()
                .filter(item -> ledgerId.equals(item.getLedgerId()))
                .map(ErpFinanceRoleSubjectDO::getSubjectCode).toList();
    }

    @Override
    public List<Long> getVisibleLedgerIdsByRoleId(Long roleId) {
        return ledgerRoleMapper.selectLedgerIdsByRoleId(roleId);
    }

    @Override
    public List<Long> getVisibleLedgerIdsByRoleIds(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return java.util.Collections.emptyList();
        }
        return ledgerRoleMapper.selectLedgerIdsByRoleIds(roleIds);
    }

    @Override
    public List<Long> getRoleIdsByLedgerId(Long ledgerId) {
        return ledgerRoleMapper.selectRoleIdsByLedgerId(ledgerId);
    }

    private void clearAffectedUserCaches(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds) || permissionService == null) {
            return;
        }
        Set<Long> userIds = permissionService.getUserRoleIdListByRoleId(roleIds);
        if (CollUtil.isEmpty(userIds)) {
            return;
        }
        userIds.forEach(this::clearUserLedgerPermissionCache);
    }

    @Override
    public void clearUserLedgerPermissionCache(Long userId) {
        if (userId == null) {
            return;
        }
        userLedgerPermissionCacheService.clearCache(userId);
        log.info("清除用户账簿权限缓存。userId={}", userId);
    }

}
