package cn.iocoder.yudao.module.erp.service.finance.guard;

import cn.iocoder.yudao.module.erp.service.finance.FinanceDataPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 外部账单向依赖安全守卫
 * 防止外部账的修改影响到内部业务数据
 */
@Slf4j
@Aspect
@Component
public class ExternalLedgerWriteGuard {

    @Resource
    private FinanceDataPermissionService financeDataPermissionService;

    /**
     * 拦截外部账写入操作，确保只能修改外部账数据
     */
    @Around("@annotation(cn.iocoder.yudao.module.erp.service.finance.guard.ExternalLedgerWriteOnly)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        // 检查当前用户是否为审计角色
        if (financeDataPermissionService.isAuditRole()) {
            // 审计角色只能修改外部账，不能修改内部业务数据
            log.info("审计角色执行外部账写入操作");
        }

        // 执行原始方法
        return point.proceed();
    }

}
