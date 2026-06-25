package cn.weitee.erp.framework.idempotent.config;

import cn.weitee.erp.framework.idempotent.core.aop.IdempotentAspect;
import cn.weitee.erp.framework.idempotent.core.keyresolver.impl.DefaultIdempotentKeyResolver;
import cn.weitee.erp.framework.idempotent.core.keyresolver.impl.ExpressionIdempotentKeyResolver;
import cn.weitee.erp.framework.idempotent.core.keyresolver.IdempotentKeyResolver;
import cn.weitee.erp.framework.idempotent.core.keyresolver.impl.UserIdempotentKeyResolver;
import cn.weitee.erp.framework.idempotent.core.redis.IdempotentRedisDAO;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import cn.weitee.erp.framework.redis.config.WeiteeRedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

@AutoConfiguration(after = WeiteeRedisAutoConfiguration.class)
public class WeiteeIdempotentConfiguration {

    @Bean
    public IdempotentAspect idempotentAspect(List<IdempotentKeyResolver> keyResolvers, IdempotentRedisDAO idempotentRedisDAO) {
        return new IdempotentAspect(keyResolvers, idempotentRedisDAO);
    }

    @Bean
    public IdempotentRedisDAO idempotentRedisDAO(StringRedisTemplate stringRedisTemplate) {
        return new IdempotentRedisDAO(stringRedisTemplate);
    }

    // ========== 各种 IdempotentKeyResolver Bean ==========

    @Bean
    public DefaultIdempotentKeyResolver defaultIdempotentKeyResolver() {
        return new DefaultIdempotentKeyResolver();
    }

    @Bean
    public UserIdempotentKeyResolver userIdempotentKeyResolver() {
        return new UserIdempotentKeyResolver();
    }

    @Bean
    public ExpressionIdempotentKeyResolver expressionIdempotentKeyResolver() {
        return new ExpressionIdempotentKeyResolver();
    }

}
