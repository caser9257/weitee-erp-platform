package cn.weitee.erp.framework.banner.core;

import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.util.ClassUtils;

import java.util.concurrent.TimeUnit;

/**
 * 项目启动成功后，提供文档相关的地址
 *
 * @author WeTai
 */
@Slf4j
public class BannerApplicationRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        ThreadUtil.execute(() -> {
            ThreadUtil.sleep(1, TimeUnit.SECONDS); // 延迟 1 秒，保证输出到结尾
            log.info("\n----------------------------------------------------------\n\t" +
                            "项目启动成功！\n\t" +
                            "----------------------------------------------------------");

            // 数据报表
            if (isNotPresent("cn.weitee.erp.module.report.framework.security.config.SecurityConfiguration")) {
                System.out.println("[报表模块 - 已禁用][请联系管理员开启]");
            }
            // 工作流
            if (isNotPresent("cn.weitee.erp.module.bpm.framework.flowable.config.BpmFlowableConfiguration")) {
                System.out.println("[工作流模块 - 已禁用][请联系管理员开启]");
            }
            // 商城系统
            if (isNotPresent("cn.weitee.erp.module.trade.framework.web.config.TradeWebConfiguration")) {
                System.out.println("[商城系统 - 已禁用][请联系管理员开启]");
            }
            // ERP 系统
            if (isNotPresent("cn.weitee.erp.module.erp.framework.web.config.ErpWebConfiguration")) {
                System.out.println("[ERP 系统 - 已禁用][请联系管理员开启]");
            }
            // WMS 仓库管理系统
            if (isNotPresent("cn.weitee.erp.module.wms.framework.web.config.WmsWebConfiguration")) {
                System.out.println("[WMS 仓库管理系统 - 已禁用][请联系管理员开启]");
            }
            // CRM 系统
            if (isNotPresent("cn.weitee.erp.module.crm.framework.web.config.CrmWebConfiguration")) {
                System.out.println("[CRM 系统 - 已禁用][请联系管理员开启]");
            }
            // MES 系统
            if (isNotPresent("cn.weitee.erp.module.mes.framework.web.config.MesWebConfiguration")) {
                System.out.println("[MES 系统 - 已禁用][请联系管理员开启]");
            }
            // AI 大模型
            if (isNotPresent("cn.weitee.erp.module.ai.framework.web.config.AiWebConfiguration")) {
                System.out.println("[AI 大模型 - 已禁用][请联系管理员开启]");
            }
            // IoT 物联网
            if (isNotPresent("cn.weitee.erp.module.iot.framework.web.config.IotWebConfiguration")) {
                System.out.println("[IoT 物联网 - 已禁用][请联系管理员开启]");
            }
            // IM 即时通讯
            if (isNotPresent("cn.weitee.erp.module.im.framework.web.config.ImWebConfiguration")) {
                System.out.println("[IM 即时通讯 - 已禁用][请联系管理员开启]");
            }
        });
    }

    private static boolean isNotPresent(String className) {
        return !ClassUtils.isPresent(className, ClassUtils.getDefaultClassLoader());
    }

}
