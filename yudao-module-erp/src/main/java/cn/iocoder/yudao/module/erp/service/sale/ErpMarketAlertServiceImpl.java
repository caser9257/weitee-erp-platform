package cn.iocoder.yudao.module.erp.service.sale;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.MarketAlertRuleVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.MarketAlertVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpMarketAlertRuleDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpMarketAlertRuleMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinanceReceiptService;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 市场预警服务实现
 *
 * @author system
 */
@Service
@Validated
@Slf4j
public class ErpMarketAlertServiceImpl implements ErpMarketAlertService {

    @Resource
    private ErpSaleOrderMapper erpSaleOrderMapper;

    @Resource
    private ErpFinanceReceiptService erpFinanceReceiptService;

    @Resource
    private ErpMarketAlertRuleMapper erpMarketAlertRuleMapper;

    @Override
    public List<MarketAlertRuleVO> getAlertRules() {
        // 从数据库读取预警规则
        List<ErpMarketAlertRuleDO> ruleDOList = erpMarketAlertRuleMapper.selectList();
        return BeanUtils.toBean(ruleDOList, MarketAlertRuleVO.class);
    }

    @Override
    public void updateAlertRule(MarketAlertRuleVO ruleVO) {
        // 更新规则到数据库
        ErpMarketAlertRuleDO ruleDO = erpMarketAlertRuleMapper.selectOne(
                ErpMarketAlertRuleDO::getRuleCode, ruleVO.getCode());
        if (ruleDO != null) {
            ruleDO.setEnabled(ruleVO.getEnabled());
            ruleDO.setThresholdDays(ruleVO.getThresholdDays());
            ruleDO.setLevel(ruleVO.getLevel());
            erpMarketAlertRuleMapper.updateById(ruleDO);
            log.info("[updateAlertRule] 更新预警规则：{}", ruleVO.getCode());
        } else {
            log.warn("[updateAlertRule] 预警规则不存在：{}", ruleVO.getCode());
        }
    }

    @Override
    public List<MarketAlertVO> getCurrentAlerts() {
        List<MarketAlertVO> alerts = new ArrayList<>();

        // 从数据库读取规则配置
        List<ErpMarketAlertRuleDO> rules = erpMarketAlertRuleMapper.selectList();
        int receiptThresholdDays = 30;
        int deliveryThresholdDays = 0;
        int releaseThresholdDays = 7;
        int invoiceThresholdDays = 30;

        for (ErpMarketAlertRuleDO rule : rules) {
            if (!Boolean.TRUE.equals(rule.getEnabled())) continue;
            switch (rule.getRuleCode()) {
                case "RECEIPT_OVERDUE":
                    receiptThresholdDays = rule.getThresholdDays() != null ? rule.getThresholdDays() : 30;
                    break;
                case "DELIVERY_OVERDUE":
                    deliveryThresholdDays = rule.getThresholdDays() != null ? rule.getThresholdDays() : 0;
                    break;
                case "RELEASE_BLOCKED":
                    releaseThresholdDays = rule.getThresholdDays() != null ? rule.getThresholdDays() : 7;
                    break;
                case "INVOICE_OVERDUE":
                    invoiceThresholdDays = rule.getThresholdDays() != null ? rule.getThresholdDays() : 30;
                    break;
            }
        }

        // 查询所有订单
        List<ErpSaleOrderDO> orders = erpSaleOrderMapper.selectList();
        LocalDate today = LocalDate.now();

        for (ErpSaleOrderDO order : orders) {
            // 1. 检查收款逾期
            if (order.getDeliveryDate() != null && order.getDeliveryDate().isBefore(today)) {
                BigDecimal receivedAmount = erpFinanceReceiptService.getReceivedAmountByOrderId(order.getId());
                BigDecimal orderAmount = order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO;

                if (receivedAmount.compareTo(orderAmount) < 0) {
                    long daysOverdue = ChronoUnit.DAYS.between(order.getDeliveryDate(), today);
                    if (daysOverdue >= receiptThresholdDays) {
                        MarketAlertVO alert = new MarketAlertVO();
                        alert.setId(order.getId());
                        alert.setRuleCode("RECEIPT_OVERDUE");
                        alert.setRuleName("收款逾期预警");
                        alert.setLevel("WARNING");
                        alert.setProjectId(order.getProjectId());
                        alert.setOrderId(order.getId());
                        alert.setOrderNo(order.getNo());
                        alert.setContent("订单交期已过" + daysOverdue + "天，仍未收到货款");
                        alert.setTriggerTime(LocalDateTime.now());
                        alert.setHandled(false);
                        alerts.add(alert);
                    }
                }
            }

            // 2. 检查交期逾期
            if (order.getDeliveryDate() != null && order.getDeliveryDate().isBefore(today)) {
                if (order.getOutCount() == null || order.getOutCount().compareTo(BigDecimal.ZERO) == 0) {
                    long daysOverdue = ChronoUnit.DAYS.between(order.getDeliveryDate(), today);
                    if (daysOverdue >= deliveryThresholdDays) {
                        MarketAlertVO alert = new MarketAlertVO();
                        alert.setId(order.getId() + 10000);
                        alert.setRuleCode("DELIVERY_OVERDUE");
                        alert.setRuleName("交期逾期预警");
                        alert.setLevel("DANGER");
                        alert.setProjectId(order.getProjectId());
                        alert.setOrderId(order.getId());
                        alert.setOrderNo(order.getNo());
                        alert.setContent("订单交期已过" + daysOverdue + "天，但未出库");
                        alert.setTriggerTime(LocalDateTime.now());
                        alert.setHandled(false);
                        alerts.add(alert);
                    }
                }
            }

            // 3. 检查放行阻塞
            if ("BLOCKED".equals(order.getShipmentReleaseStatus())) {
                MarketAlertVO alert = new MarketAlertVO();
                alert.setId(order.getId() + 20000);
                alert.setRuleCode("RELEASE_BLOCKED");
                alert.setRuleName("放行阻塞预警");
                alert.setLevel("WARNING");
                alert.setProjectId(order.getProjectId());
                alert.setOrderId(order.getId());
                alert.setOrderNo(order.getNo());
                alert.setContent("订单放行状态为阻塞：" + (order.getShipmentReleaseReason() != null ? order.getShipmentReleaseReason() : "未知原因"));
                alert.setTriggerTime(LocalDateTime.now());
                alert.setHandled(false);
                alerts.add(alert);
            }

            // 4. 检查开票逾期
            if (order.getOutCount() != null && order.getOutCount().compareTo(BigDecimal.ZERO) > 0
                    && "NOT_INVOICED".equals(order.getInvoiceStatus())) {
                // 出库后超过阈值天数未开票
                if (order.getUpdateTime() != null) {
                    long daysSinceOutbound = ChronoUnit.DAYS.between(
                            order.getUpdateTime().toLocalDate(), today);
                    if (daysSinceOutbound >= invoiceThresholdDays) {
                        MarketAlertVO alert = new MarketAlertVO();
                        alert.setId(order.getId() + 30000);
                        alert.setRuleCode("INVOICE_OVERDUE");
                        alert.setRuleName("开票逾期预警");
                        alert.setLevel("WARNING");
                        alert.setProjectId(order.getProjectId());
                        alert.setOrderId(order.getId());
                        alert.setOrderNo(order.getNo());
                        alert.setContent("出库后已过" + daysSinceOutbound + "天，仍未开票");
                        alert.setTriggerTime(LocalDateTime.now());
                        alert.setHandled(false);
                        alerts.add(alert);
                    }
                }
            }
        }

        return alerts;
    }

    @Override
    public int checkAndTriggerAlerts() {
        List<MarketAlertVO> alerts = getCurrentAlerts();

        // 记录预警日志
        if (!alerts.isEmpty()) {
            log.info("[checkAndTriggerAlerts] 发现 {} 条预警：", alerts.size());
            for (MarketAlertVO alert : alerts) {
                log.info("  - [{}] {}：{}", alert.getLevel(), alert.getRuleName(), alert.getContent());
            }

            // TODO: 对接通知服务发送预警通知（站内信、邮件等）
            // 通知内容模板化：预警类型 + 涉及单据 + 建议操作
        } else {
            log.info("[checkAndTriggerAlerts] 未发现预警");
        }

        return alerts.size();
    }

}
