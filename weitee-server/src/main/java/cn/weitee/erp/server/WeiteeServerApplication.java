package cn.weitee.erp.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目的启动类
 *
 * @author WeTai
 */
@SuppressWarnings("SpringComponentScan") // 忽略 IDEA 无法识别 ${weitee.info.base-package}
@SpringBootApplication(scanBasePackages = {"${weitee.info.base-package}.server", "${weitee.info.base-package}.module"})
public class WeiteeServerApplication {

    public static void main(String[] args) {

        SpringApplication.run(WeiteeServerApplication.class, args);
//        new SpringApplicationBuilder(WeiteeServerApplication.class)
//                .applicationStartup(new BufferingApplicationStartup(20480))
//                .run(args);

    }

}
