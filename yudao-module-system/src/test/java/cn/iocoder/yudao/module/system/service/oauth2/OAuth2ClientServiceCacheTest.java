package cn.iocoder.yudao.module.system.service.oauth2;

import cn.iocoder.yudao.framework.redis.config.YudaoCacheAutoConfiguration;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbAndRedisUnitTest;
import cn.iocoder.yudao.module.system.dal.dataobject.oauth2.OAuth2ClientDO;
import cn.iocoder.yudao.module.system.dal.mysql.oauth2.OAuth2ClientMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * {@link OAuth2ClientServiceImpl} 缓存行为测试
 */
@Import({OAuth2ClientServiceImpl.class, YudaoCacheAutoConfiguration.class})
public class OAuth2ClientServiceCacheTest extends BaseDbAndRedisUnitTest {

    @Resource
    private OAuth2ClientService oauth2ClientService;

    @Resource
    private OAuth2ClientMapper oauth2ClientMapper;

    @Test
    public void testGetOAuth2ClientFromCache_success() {
        // mock 数据
        OAuth2ClientDO clientDO = randomPojo(OAuth2ClientDO.class);
        oauth2ClientMapper.insert(clientDO);

        // 调用，并断言不会因为 SpEL 取不到参数名导致缓存 key 为空
        OAuth2ClientDO result = assertDoesNotThrow(() ->
                oauth2ClientService.getOAuth2ClientFromCache(clientDO.getClientId()));
        assertPojoEquals(clientDO, result);
    }

}
