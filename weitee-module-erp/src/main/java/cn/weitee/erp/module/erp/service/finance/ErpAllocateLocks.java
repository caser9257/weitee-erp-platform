package cn.weitee.erp.module.erp.service.finance;

import org.redisson.api.RLock;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AP 核销互斥锁的统一获取/释放行为。
 * 付款审批、付款作废、预付款核销、预付款核销回滚必须共用本类，
 * 保证临界区（单事务、毫秒级）竞争时短暂排队而不是瞬时误报失败。
 */
final class ErpAllocateLocks {

    private static final long WAIT_SECONDS = 3;

    private ErpAllocateLocks() {
    }

    /**
     * 带超时获取锁；被中断时恢复中断标记并按获取失败处理（调用方会释放已持有的锁并抛业务异常）。
     */
    static boolean acquire(RLock lock) {
        try {
            return lock.tryLock(WAIT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    static void unlockAll(List<RLock> locks) {
        for (int index = locks.size() - 1; index >= 0; index--) {
            RLock lock = locks.get(index);
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
