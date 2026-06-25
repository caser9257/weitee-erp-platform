package cn.weitee.erp.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerRoleDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceLedgerRoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;

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
        // 删除旧关联
        ledgerRoleMapper.delete(new cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX<ErpFinanceLedgerRoleDO>()
                .eq(ErpFinanceLedgerRoleDO::getLedgerId, ledgerId));

        // 创建新关联
        if (CollUtil.isNotEmpty(roleIds)) {
            for (Long roleId : roleIds) {
                createLedgerRole(ledgerId, roleId);
            }
        }

        // 清除相关用户的缓存（简化处理，实际应该只清除相关角色的用户缓存）
        log.info("账簿角色关联变更，需要清除相关用户缓存。ledgerId={}", ledgerId);
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

    @Override
    public void clearUserLedgerPermissionCache(Long userId) {
        // TODO: 实现 Redis 缓存清除逻辑
        log.info("清除用户账簿权限缓存。userId={}", userId);
    }

}
