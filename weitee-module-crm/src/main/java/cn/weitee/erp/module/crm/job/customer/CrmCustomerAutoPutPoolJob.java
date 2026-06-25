package cn.weitee.erp.module.crm.job.customer;

import cn.weitee.erp.framework.quartz.core.handler.JobHandler;
import cn.weitee.erp.module.crm.service.customer.CrmCustomerService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 客户自动掉入公海 Job
 *
 * @author WeTai
 */
@Component
public class CrmCustomerAutoPutPoolJob implements JobHandler {

    @Resource
    private CrmCustomerService customerService;

    @Override
    public String execute(String param) {
        int count = customerService.autoPutCustomerPool();
        return String.format("掉入公海客户 %s 个", count);
    }

}
