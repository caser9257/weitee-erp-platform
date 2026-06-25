package cn.weitee.erp.module.erp.service.finance.interceptor;

import java.util.List;

/**
 * 财务数据权限上下文
 * 用于在线程中传递数据权限信息
 */
public class FinanceDataPermissionContext {

    private static final ThreadLocal<List<Long>> VISIBLE_LEDGER_IDS = new ThreadLocal<>();

    /**
     * 设置当前线程可见的账簿ID列表
     */
    public static void setVisibleLedgerIds(List<Long> ledgerIds) {
        VISIBLE_LEDGER_IDS.set(ledgerIds);
    }

    /**
     * 获取当前线程可见的账簿ID列表
     * @return 账簿ID列表，null表示不限制
     */
    public static List<Long> getVisibleLedgerIds() {
        return VISIBLE_LEDGER_IDS.get();
    }

    /**
     * 清除当前线程的数据权限上下文
     */
    public static void clear() {
        VISIBLE_LEDGER_IDS.remove();
    }

}
