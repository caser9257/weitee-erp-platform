package cn.iocoder.yudao.module.erp.util;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * ERP 事务工具类
 *
 * @author ruoyi-vue-pro
 */
public class ErpTransactionUtils {

    private ErpTransactionUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 在事务提交后执行回调
     *
     * 使用场景：需要在事务提交后触发异步操作（如事件发布），避免读到未提交的数据
     *
     * @param action 事务提交后要执行的操作
     */
    public static void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        action.run();
                    }
                }
            );
        } else {
            // 如果没有活跃的事务同步，直接执行
            action.run();
        }
    }
}
