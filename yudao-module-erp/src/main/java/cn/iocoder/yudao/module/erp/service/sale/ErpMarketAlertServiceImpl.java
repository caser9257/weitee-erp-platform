package cn.iocoder.yudao.module.erp.service.sale;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.MarketAlertRuleVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.MarketAlertVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinanceReceiptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
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

    @Override
    public List<MarketAlertRuleVO> getAlertRules() {
        // 返回预定义的预警规则
        List<MarketAlertRuleVO> rules = new ArrayList<>();

        MarketAlertRuleVO receiptOverdue = new MarketAlertRuleVO();
        receiptOverdue.setCode("RECEIPT_OVERDUE");
        receiptOverdue.setName("收款逾期预警");
        receiptOverdue.setDescription("订单交期后仍未收到货款");
        receiptOverdue.setEnabled(true);
        receiptOverdue.setThresholdDays(30);
        receiptOverdue.setLevel("WARNING");
        rules.add(receiptOverdue);

        MarketAlertRuleVO deliveryOverdue = new MarketAlertRuleVO();
        deliveryOverdue.setCode("DELIVERY_OVERDUE");
        deliveryOverdue.setName("交期逾期预警");
        deliveryOverdue.setDescription("订单交期已过但未出库");
        deliveryOverdue.setEnabled(true);
        deliveryOverdue.setThresholdDays(0);
        deliveryOverdue.setLevel("DANGER");
        rules.add(deliveryOverdue);

        MarketAlertRuleVO releaseBlocked = new MarketAlertRuleVO();
        releaseBlocked.setCode("RELEASE_BLOCKED");
        releaseBlocked.setName("放行阻塞预警");
        releaseBlocked.setDescription("订单放行状态为阻塞");
        releaseBlocked.setEnabled(true);
        releaseBlocked.setThresholdDays(7);
        releaseBlocked.setLevel("WARNING");
        rules.add(releaseBlocked);

        MarketAlertRuleVO invoiceOverdue = new MarketAlertRuleVO();
        invoiceOverdue.setCode("INVOICE_OVERDUE");
        invoiceOverdue.setName("开票逾期预警");
        invoiceOverdue.setDescription("出库后30天仍未开票");
        invoiceOverdue.setEnabled(true);
        invoiceOverdue.setThresholdDays(30);
        invoiceOverdue.setLevel("WARNING");
        rules.add(invoiceOverdue);

        return rules;
    }

    @Override
    public void updateAlertRule(MarketAlertRuleVO ruleVO) {
        // TODO: 实现规则更新（需要持久化存储）
        log.info("更新预警规则：{}", ruleVO.getCode());
    }

    @Override
    public List<MarketAlertVO> getCurrentAlerts() {
        List<MarketAlertVO> alerts = new ArrayList<>();

        // 查询所有订单
        List<ErpSaleOrderDO> orders = erpSaleOrderMapper.selectList();
        LocalDate today = LocalDate.now();

        for (ErpSaleOrderDO order : orders) {
            // 检查收款逾期
            if (order.getDeliveryDate() != null && order.getDeliveryDate().isBefore(today)) {
                BigDecimal receivedAmount = erpFinanceReceiptService.getReceivedAmountByOrderId(order.getId());
                BigDecimal orderAmount = order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO;
                
                if (receivedAmount.compareTo(orderAmount) < 0) {
                    long daysOverdue = ChronoUnit.DAYS.between(order.getDeliveryDate(), today);
                    if (daysOverdue >= 30) {
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

            // 检查交期逾期
            if (order.getDeliveryDate() != null && order.getDeliveryDate().isBefore(today)) {
                if (order.getOutCount() == null || order.getOutCount().compareTo(BigDecimal.ZERO) == 0) {
                    long daysOverdue = ChronoUnit.DAYS.between(order.getDeliveryDate(), today);
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

            // 检查放行阻塞
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
        }

        return alerts;
    }

    @Override
    public int checkAndTriggerAlerts() {
        List<MarketAlertVO> alerts = getCurrentAlerts();
        // TODO: 实现预警通知（邮件、站内信等）
        log.info("检查预警完成，发现 {} 条预警", alerts.size());
        return alerts.size();
    }

}
