package cn.iocoder.yudao.module.bpm.service.approval;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;

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
        assertTrue(environment.containsProperty("yudao.info.base-package"));
        assertTrue(applicationContext.containsBean("dataSource"));
        assertTrue(applicationContext.containsBean("transactionManager"));
        assertTrue(applicationContext.containsBean("sqlSessionFactory"));
    }

}
