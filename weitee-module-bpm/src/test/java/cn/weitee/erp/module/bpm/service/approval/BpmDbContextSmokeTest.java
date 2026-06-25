package cn.weitee.erp.module.bpm.service.approval;

import cn.weitee.erp.framework.test.core.ut.BaseDbUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import jakarta.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BpmDbContextSmokeTest extends BaseDbUnitTest {

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private Environment environment;

    @Test
    void testMybatisInfrastructureExists() {
        assertNotNull(applicationContext);
        assertNotNull(environment);
        assertTrue(environment.containsProperty("spring.datasource.url"));
        assertTrue(environment.containsProperty("weitee.info.base-package"));
        assertTrue(applicationContext.containsBean("dataSource"));
        assertTrue(applicationContext.containsBean("transactionManager"));
        assertTrue(applicationContext.containsBean("sqlSessionFactory"));
    }

}
