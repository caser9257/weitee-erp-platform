package cn.iocoder.yudao.module.erp.service.sale;

import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.MarketAlertRuleVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.MarketAlertVO;

import java.util.List;

/**
 * 市场预警服务接口
 *
 * @author system
 */
public interface ErpMarketAlertService {

    /**
     * 获取预警规则列表
     *
     * @return 预警规则列表
     */
    List<MarketAlertRuleVO> getAlertRules();

    /**
     * 更新预警规则
     *
     * @param ruleVO 规则信息
     */
    void updateAlertRule(MarketAlertRuleVO ruleVO);

    /**
     * 获取当前预警列表
     *
     * @return 预警列表
     */
    List<MarketAlertVO> getCurrentAlerts();

    /**
     * 检查并触发预警
     *
     * @return 新触发的预警数量
     */
    int checkAndTriggerAlerts();

}
