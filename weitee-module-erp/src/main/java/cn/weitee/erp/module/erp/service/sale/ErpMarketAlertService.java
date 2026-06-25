package cn.weitee.erp.module.erp.service.sale;

import cn.weitee.erp.module.erp.controller.admin.sale.vo.MarketAlertRuleVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.MarketAlertVO;

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
     * 获取当前预警列表（实时计算 + 历史记录）
     *
     * @return 预警列表
     */
    List<MarketAlertVO> getCurrentAlerts();

    /**
     * 获取预警历史记录
     *
     * @return 预警历史列表
     */
    List<MarketAlertVO> getAlertHistory();

    /**
     * 检查并触发预警（持久化到数据库）
     *
     * @return 新触发的预警数量
     */
    int checkAndTriggerAlerts();

    /**
     * 处理预警
     *
     * @param alertId 预警ID
     * @param handleRemark 处理备注
     */
    void handleAlert(Long alertId, String handleRemark);

}
