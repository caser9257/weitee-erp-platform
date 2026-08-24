package cn.weitee.erp.module.erp.service.finance.interceptor;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * 财务数据权限的完整范围。
 */
public record FinancePermissionScope(
        Scope<Long> ledgerScope,
        Scope<Long> deptScope,
        Map<Long, Scope<String>> subjectScopesByLedger,
        boolean auditOnly,
        boolean readOnly) {

    public FinancePermissionScope {
        subjectScopesByLedger = subjectScopesByLedger == null
                ? Collections.emptyMap() : Map.copyOf(subjectScopesByLedger);
    }

    public static FinancePermissionScope ofLedgerScope(Scope<Long> ledgerScope, boolean auditOnly) {
        return new FinancePermissionScope(ledgerScope, Scope.all(), Collections.emptyMap(), auditOnly, auditOnly);
    }

    public enum ScopeMode {
        ALL, LIMITED, NONE
    }

    public record Scope<T>(ScopeMode mode, Set<T> values) {

        public Scope {
            values = values == null ? Collections.emptySet() : Set.copyOf(values);
            if (mode == ScopeMode.LIMITED && values.isEmpty()) {
                throw new IllegalArgumentException("LIMITED 权限范围不能为空");
            }
        }

        public static <T> Scope<T> all() {
            return new Scope<>(ScopeMode.ALL, Collections.emptySet());
        }

        public static <T> Scope<T> none() {
            return new Scope<>(ScopeMode.NONE, Collections.emptySet());
        }

        public static <T> Scope<T> limited(Set<T> values) {
            return new Scope<>(ScopeMode.LIMITED, values);
        }
    }
}
