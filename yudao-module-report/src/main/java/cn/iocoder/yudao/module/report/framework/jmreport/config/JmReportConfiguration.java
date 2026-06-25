package cn.iocoder.yudao.module.report.framework.jmreport.config;

import cn.iocoder.yudao.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import cn.iocoder.yudao.framework.common.biz.system.permission.PermissionCommonApi;
import cn.iocoder.yudao.framework.security.config.SecurityProperties;
import cn.iocoder.yudao.module.report.framework.jmreport.core.service.JmOnlDragExternalServiceImpl;
import cn.iocoder.yudao.module.report.framework.jmreport.core.service.JmReportTokenServiceImpl;
import jakarta.servlet.Filter;
import org.jeecg.modules.jmreport.api.JmReportTokenServiceI;
import org.jeecg.modules.jmreport.common.interceptor.JimuReportSignatureInterceptor;
import org.jeecg.modules.jmreport.common.util.JimuI18nUtils;
import org.jeecg.modules.jmreport.config.firewall.interceptor.JimuReportTokenInterceptor;
import org.jeecg.modules.jmreport.config.firewall.interceptor.handlers.JmIPermissionsVerifyHandler;
import org.jeecg.modules.jmreport.config.init.JimuReportConfiguration;
import org.jeecg.modules.jmreport.config.locale.JimuLocaleInterceptor;
import org.jeecg.modules.jmreport.config.locale.JimuLocaleResolver;
import org.jeecg.modules.jmreport.config.oss.JmReportUploadConfig;
import org.jeecg.modules.jmreport.desreport.render.echarts.IJimuEchartsRender;
import org.jeecg.modules.jmreport.desreport.render.echarts.model.JimuEchartsRenderParams;
import org.jeecg.modules.jmreport.desreport.service.IJmreportNoSqlService;
import org.jeecg.modules.jmreport.dyndb.vo.JmreportDynamicDataSourceVo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.CacheControl;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 积木报表的配置类
 *
 * @author WeTai
 */
@Configuration(proxyBeanMethods = false)
@PropertySource("classpath:config/default-config.properties")
@ComponentScan(
        basePackages = "org.jeecg.modules.jmreport", // 扫描积木报表的包
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JimuReportConfiguration.class))
public class JmReportConfiguration implements WebMvcConfigurer {

    private final JmReportUploadConfig jmReportUploadConfig;

    public JmReportConfiguration(JmReportUploadConfig jmReportUploadConfig) {
        this.jmReportUploadConfig = jmReportUploadConfig;
    }

    JmReportConfiguration() {
        this(null);
    }

    @Bean
    public JmReportTokenServiceI jmReportTokenService(OAuth2TokenCommonApi oAuth2TokenApi,
                                                      PermissionCommonApi permissionApi,
                                                      SecurityProperties securityProperties) {
        return new JmReportTokenServiceImpl(oAuth2TokenApi, permissionApi, securityProperties);
    }

    @Bean // 暂时注释：可以按需实现后打开
    @Primary
    public JmOnlDragExternalServiceImpl jmOnlDragExternalService2() {
        return new JmOnlDragExternalServiceImpl();
    }

    @Bean("jmPermissionsVerifyHandler")
    public JmIPermissionsVerifyHandler jmPermissionsVerifyHandler() {
        return new JmIPermissionsVerifyHandler();
    }

    @Bean
    public JimuReportTokenInterceptor jimuReportInterceptor() {
        return new JimuReportTokenInterceptor();
    }

    @Bean
    public JimuReportSignatureInterceptor jmSignatureInterceptor() {
        return new JimuReportSignatureInterceptor();
    }

    public Filter jmFilter() {
        return new org.jeecg.modules.jmreport.common.a.a();
    }

    @Bean
    public FilterRegistrationBean<Filter> jmFilterRegistration() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(jmFilter());
        registration.setName("jmFilter");
        registration.addUrlPatterns(
                "/jmreport/queryFieldBySql",
                "/jmreport/loadTableData",
                "/jmreport/show",
                "/jmreport/exportPdfStream",
                "/jmreport/exportAllExcelStream",
                "/jmreport/qurestSql",
                "/jmreport/map/queryMapByCode",
                "/jmreport/qurestApi",
                "/jmreport/getCharData",
                "/jmreport/testConnection",
                "/jmreport/save",
                "/jmreport/form/submit",
                "/jmreport/getQueryInfo");
        return registration;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] staticPatterns = {
                "/*.js", "/*.css", "/*.svg", "/*.pdf", "/*.jpg", "/*.png",
                "/*.ico", "/*.html", "/html/**", "/js/**", "/css/**", "/images/**"
        };
        registry.addInterceptor(jimuReportInterceptor())
                .excludePathPatterns(staticPatterns)
                .addPathPatterns("/jmreport/**", "/drag/**");
        registry.addInterceptor(jmSignatureInterceptor())
                .excludePathPatterns(staticPatterns)
                .addPathPatterns("/jmreport/**", "/drag/**");
        try {
            registry.addInterceptor(jimuLocaleInterceptor());
        } catch (Exception ignored) {
            // 与 JimuReport 原配置保持一致：未启用 i18n 时不注册 Locale 拦截器。
        }
    }

    @Override
    public void addResourceHandlers(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/jmreport/desreport_/**")
                .addResourceLocations("classpath:/static/jmreport/desreport_/")
                .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS));
        registry.addResourceHandler("/drag/lib/**")
                .addResourceLocations("classpath:/static/drag/lib/")
                .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS));
        registry.addResourceHandler("/drag/list/**")
                .addResourceLocations("classpath:/static/drag/list/")
                .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS));
        if (jmReportUploadConfig != null && jmReportUploadConfig.getPath() != null) {
            registry.addResourceHandler("/jmreport/img/**")
                    .addResourceLocations("file:" + jmReportUploadConfig.getPath().getUpload() + "/")
                    .setCacheControl(CacheControl.maxAge(10, TimeUnit.DAYS));
        }
    }

    @Bean("jmTaskScheduler")
    public TaskScheduler jmTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(8);
        scheduler.setThreadNamePrefix("scheduled-thread-jm-");
        return scheduler;
    }

    @Bean("jmreportNoSqlServiceImpl")
    @ConditionalOnMissingBean(IJmreportNoSqlService.class)
    public IJmreportNoSqlService defaultJmreportNoSqlService() {
        return new IJmreportNoSqlService() {

            @Override
            public List<Map<String, Object>> findList(String collectionName, String json) {
                return Collections.emptyList();
            }

            @Override
            public Boolean testConnection(JmreportDynamicDataSourceVo jmreportDynamicDataSourceVo) {
                return false;
            }

        };
    }

    @Bean("jimuI18nUtils")
    @ConditionalOnProperty(name = "jeecg.jmreport.i18n.enable", havingValue = "true")
    public JimuI18nUtils jimuI18nUtils() {
        return new JimuI18nUtils();
    }

    @Bean("jimuLocaleInterceptor")
    @ConditionalOnProperty(name = "jeecg.jmreport.i18n.enable", havingValue = "true")
    public JimuLocaleInterceptor jimuLocaleInterceptor() {
        return new JimuLocaleInterceptor(new JimuLocaleResolver());
    }

    @Bean("jimuEchartsRender")
    @ConditionalOnMissingBean(IJimuEchartsRender.class)
    public IJimuEchartsRender defaultIJimuEchartsRender() {
        return params -> params.getConfig() == null ? "{}" : params.getConfig().toJSONString();
    }

}
