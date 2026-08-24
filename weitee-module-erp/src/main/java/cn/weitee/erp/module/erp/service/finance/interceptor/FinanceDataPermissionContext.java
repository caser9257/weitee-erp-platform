package cn.weitee.erp.module.erp.service.finance.interceptor;

import java.util.Collections;
import java.util.List;

/**
 * 财务数据权限上下文
 * 用于在线程中传递数据权限信息
 */
public class FinanceDataPermissionContext {

    private static final ThreadLocal<FinancePermissionScope> PERMISSION_SCOPE = new ThreadLocal<>();

    /**
     * 设置当前线程可见的账簿ID列表
     */
    public static void setVisibleLedgerIds(List<Long> ledgerIds) {
        FinancePermissionScope.Scope<Long> ledgerScope = ledgerIds == null
                ? FinancePermissionScope.Scope.all()
                : ledgerIds.isEmpty() ? FinancePermissionScope.Scope.none()
                : FinancePermissionScope.Scope.limited(new java.util.HashSet<>(ledgerIds));
        setPermissionScope(FinancePermissionScope.ofLedgerScope(ledgerScope, false));
    }

    /**
     * 设置当前线程的完整财务权限范围。
     */
    public static void setPermissionScope(FinancePermissionScope permissionScope) {
        PERMISSION_SCOPE.set(permissionScope);
    }

    /**
     * 获取当前线程的完整财务权限范围。
     */
    public static FinancePermissionScope getPermissionScope() {
        return PERMISSION_SCOPE.get();
    }

    /**
     * 获取当前线程可见的账簿ID列表
     * @return 账簿ID列表，null表示不限制
     */
    public static List<Long> getVisibleLedgerIds() {
        FinancePermissionScope permissionScope = getPermissionScope();
        if (permissionScope == null || permissionScope.ledgerScope().mode() == FinancePermissionScope.ScopeMode.ALL) {
            return null;
        }
        return permissionScope.ledgerScope().mode() == FinancePermissionScope.ScopeMode.NONE
                ? Collections.emptyList() : List.copyOf(permissionScope.ledgerScope().values());
    }

    /**
     * 清除当前线程的数据权限上下文
     */
    public static void clear() {
        PERMISSION_SCOPE.remove();
    }

}
