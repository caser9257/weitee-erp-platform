package cn.weitee.erp.framework.banner.config;

import cn.weitee.erp.framework.banner.core.BannerApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Banner 的自动配置类
 *
 * @author WeTai
 */
@AutoConfiguration
public class WeiteeBannerAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "weitee.banner", name = "enable", havingValue = "true", matchIfMissing = true)
    public BannerApplicationRunner bannerApplicationRunner() {
        return new BannerApplicationRunner();
    }

}
