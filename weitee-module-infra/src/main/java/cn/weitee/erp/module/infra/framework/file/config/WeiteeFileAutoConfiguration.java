package cn.weitee.erp.module.infra.framework.file.config;

import cn.weitee.erp.module.infra.framework.file.core.client.FileClientFactory;
import cn.weitee.erp.module.infra.framework.file.core.client.FileClientFactoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件配置类
 *
 * @author WeTai
 */
@Configuration(proxyBeanMethods = false)
public class WeiteeFileAutoConfiguration {

    @Bean
    public FileClientFactory fileClientFactory() {
        return new FileClientFactoryImpl();
    }

}
