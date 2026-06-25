package cn.weitee.erp.module.erp.service.stock;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ErpStockServiceImplTest {

    @Test
    void updateStockCountIncrement_shouldDeclareTransactionalBoundary() throws Exception {
        Method method = ErpStockServiceImpl.class.getMethod("updateStockCountIncrement",
                Long.class, Long.class, java.math.BigDecimal.class);
        assertTrue(method.isAnnotationPresent(Transactional.class),
                "updateStockCountIncrement should declare @Transactional");
    }

}
