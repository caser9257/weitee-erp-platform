package cn.iocoder.yudao.module.erp.service.finance;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ErpAccountServiceImplTest {

    @Test
    void updateAccountDefaultStatus_shouldDeclareTransactionalBoundary() throws Exception {
        Method method = ErpAccountServiceImpl.class.getMethod("updateAccountDefaultStatus", Long.class, Boolean.class);
        assertTrue(method.isAnnotationPresent(Transactional.class),
                "updateAccountDefaultStatus should declare @Transactional");
    }

}
