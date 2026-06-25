package cn.weitee.erp.module.erp.service.finance.interceptor;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.module.erp.service.finance.FinanceDataPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 财务数据权限拦截器
 * 通过 AOP 自动注入账簿过滤条件
 */
@Slf4j
@Aspect
@Component
public class FinanceDataPermissionInterceptor {

    @Resource
    private FinanceDataPermissionService financeDataPermissionService;

    /**
     * 拦截财务查询方法，自动注入数据权限过滤
     */
    @Around("@annotation(cn.weitee.erp.module.erp.service.finance.interceptor.FinanceDataPermission)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        // 获取当前用户可见的账簿ID列表
        List<Long> visibleLedgerIds = financeDataPermissionService.getVisibleLedgerIds();

        // 将可见账簿ID列表设置到上下文
        FinanceDataPermissionContext.setVisibleLedgerIds(visibleLedgerIds);

        try {
            return point.proceed();
        } finally {
            // 清除上下文
            FinanceDataPermissionContext.clear();
        }
    }

}
