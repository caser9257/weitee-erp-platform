package cn.weitee.erp.module.erp.framework.web.config;

import cn.weitee.erp.framework.swagger.config.WeiteeSwaggerAutoConfiguration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * erp 模块的 web 组件的 Configuration
 *
 * @author WeTai
 */
@Configuration(proxyBeanMethods = false)
public class ErpWebConfiguration {

    /**
     * erp 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi erpGroupedOpenApi() {
        return WeiteeSwaggerAutoConfiguration.buildGroupedOpenApi("erp");
    }

}
