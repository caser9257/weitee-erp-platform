package cn.iocoder.yudao.module.report.framework.jmreport.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class JmReportConfigurationTest {

    @Test
    void jmFilter_shouldNotPrintJimuReportBanner() {
        JmReportConfiguration configuration = new JmReportConfiguration();
        PrintStream originalOut = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));

            configuration.jmFilter();
        } finally {
            System.setOut(originalOut);
        }

        assertThat(out.toString(StandardCharsets.UTF_8)).doesNotContain("JimuReport");
    }

    @Test
    void jmFilter_shouldOnlyBeRegisteredByFilterRegistrationBean() throws NoSuchMethodException {
        Method jmFilter = JmReportConfiguration.class.getDeclaredMethod("jmFilter");

        assertThat(jmFilter.getAnnotation(Bean.class)).isNull();
    }

    @Test
    void componentScan_shouldExcludeOriginalJimuReportConfiguration() {
        ComponentScan componentScan = JmReportConfiguration.class.getAnnotation(ComponentScan.class);

        assertThat(componentScan.basePackages()).contains("org.jeecg.modules.jmreport");
        assertThat(Arrays.stream(componentScan.excludeFilters())
                .anyMatch(filter -> filter.type() == FilterType.ASSIGNABLE_TYPE
                        && Arrays.asList(filter.classes())
                        .contains(org.jeecg.modules.jmreport.config.init.JimuReportConfiguration.class)))
                .isTrue();
    }

}
