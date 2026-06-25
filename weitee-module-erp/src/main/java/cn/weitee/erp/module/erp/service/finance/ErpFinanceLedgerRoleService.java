package cn.weitee.erp.module.erp.service.finance;

import java.util.List;

/**
 * ERP 账簿角色关联 Service 接口
 */
public interface ErpFinanceLedgerRoleService {

    /**
     * 创建账簿角色关联
     *
     * @param ledgerId 账簿ID
     * @param roleId   角色ID
     * @return 关联ID
     */
    Long createLedgerRole(Long ledgerId, Long roleId);

    /**
     * 删除账簿角色关联
     *
     * @param ledgerId 账簿ID
     * @param roleId   角色ID
     */
    void deleteLedgerRole(Long ledgerId, Long roleId);

    /**
     * 设置账簿的可见角色列表
     *
     * @param ledgerId 账簿ID
     * @param roleIds  角色ID列表
     */
    void setLedgerRoles(Long ledgerId, List<Long> roleIds);

    /**
     * 获取指定角色可见的账簿ID列表
     *
     * @param roleId 角色ID
     * @return 账簿ID列表
     */
    List<Long> getVisibleLedgerIdsByRoleId(Long roleId);

    /**
     * 获取指定角色集合可见的账簿ID列表
     *
     * @param roleIds 角色ID列表
     * @return 账簿ID列表
     */
    List<Long> getVisibleLedgerIdsByRoleIds(List<Long> roleIds);

    /**
     * 获取指定账簿的可见角色ID列表
     *
     * @param ledgerId 账簿ID
     * @return 角色ID列表
     */
    List<Long> getRoleIdsByLedgerId(Long ledgerId);

    /**
     * 清除指定用户的账簿权限缓存
     *
     * @param userId 用户ID
     */
    void clearUserLedgerPermissionCache(Long userId);

}
