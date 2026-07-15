package cn.weitee.erp.module.erp.service.finance;

import java.util.List;

/**
 * 财务数据权限服务接口
 * 用于控制用户对财务账簿的访问权限
 */
public interface FinanceDataPermissionService {

    /**
     * 获取当前用户可见的账簿ID列表
     *
     * @return 账簿ID列表，如果返回null表示不限制（可看所有账簿）
     */
    List<Long> getVisibleLedgerIds();

    /**
     * 获取指定用户可见的账簿ID列表
     *
     * @param userId 用户ID
     * @return 账簿ID列表，如果返回null表示不限制（可看所有账簿）
     */
    List<Long> getVisibleLedgerIds(Long userId);

    /**
     * 检查当前用户是否可以访问指定账簿
     *
     * @param ledgerId 账簿ID
     * @return 是否可访问
     */
    boolean canAccessLedger(Long ledgerId);

    /**
     * 检查指定用户是否可以访问指定账簿
     */
    boolean canAccessLedger(Long userId, Long ledgerId);

    /**
     * 检查指定用户是否可以访问双账套
     */
    boolean canAccessDualLedger(Long userId, Integer bizType);

    /**
     * 检查当前用户是否为审计角色
     *
     * @return 是否为审计角色
     */
    boolean isAuditRole();

    /**
     * 获取当前用户可见的部门ID列表（用于部门级数据权限）
     *
     * @return 部门ID列表，如果返回null表示不限制
     */
    List<Long> getVisibleDeptIds();

    /**
     * 获取当前用户可见的科目代码列表（用于科目级数据权限）
     *
     * @return 科目代码列表，如果返回null表示不限制
     */
    List<String> getVisibleSubjectCodes();

}
