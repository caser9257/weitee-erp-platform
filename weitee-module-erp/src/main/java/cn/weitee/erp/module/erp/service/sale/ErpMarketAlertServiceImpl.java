package cn.weitee.erp.module.erp.service.sale;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.MarketAlertRuleVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.MarketAlertVO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpMarketAlertRecordDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpMarketAlertRuleDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpMarketAlertRecordMapper;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpMarketAlertRuleMapper;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceReceiptService;
import cn.weitee.erp.module.system.api.notify.NotifyMessageSendApi;
import cn.weitee.erp.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.MARKET_ALERT_NOT_EXISTS;

/**
 * 市场预警服务实现
 *
 * @author system
 */
@Service
@Validated
@Slf4j
public class ErpMarketAlertServiceImpl implements ErpMarketAlertService {

    /** 预警阈值默认值 */
    private static final int DEFAULT_RECEIPT_THRESHOLD_DAYS = 30;
    private static final int DEFAULT_DELIVERY_THRESHOLD_DAYS = 0;
    private static final int DEFAULT_RELEASE_THRESHOLD_DAYS = 7;
    private static final int DEFAULT_INVOICE_THRESHOLD_DAYS = 30;

    @Resource
    private ErpSaleOrderMapper erpSaleOrderMapper;

    @Resource
    private ErpFinanceReceiptService erpFinanceReceiptService;

    @Resource
    private ErpMarketAlertRuleMapper erpMarketAlertRuleMapper;

    @Resource
    private ErpMarketAlertRecordMapper erpMarketAlertRecordMapper;

    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    @Override
    public List<MarketAlertRuleVO> getAlertRules() {
        List<ErpMarketAlertRuleDO> ruleDOList = erpMarketAlertRuleMapper.selectList();
        return ruleDOList.stream().map(ruleDO -> {
            MarketAlertRuleVO ruleVO = new MarketAlertRuleVO();
            ruleVO.setCode(ruleDO.getRuleCode());
            ruleVO.setName(ruleDO.getRuleName());
            ruleVO.setDescription(ruleDO.getDescription());
            ruleVO.setEnabled(ruleDO.getEnabled());
            ruleVO.setThresholdDays(ruleDO.getThresholdDays());
            ruleVO.setLevel(ruleDO.getLevel());
            return ruleVO;
        }).toList();
    }

    @Override
    public void updateAlertRule(MarketAlertRuleVO ruleVO) {
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
        // 1. 获取未处理的历史预警记录
        List<ErpMarketAlertRecordDO> records = erpMarketAlertRecordMapper.selectList(
                ErpMarketAlertRecordDO::getHandled, false);

        // 2. 转换为VO
        return convertToAlertVOList(records);
    }

    @Override
    public List<MarketAlertVO> getAlertHistory() {
        List<ErpMarketAlertRecordDO> records = erpMarketAlertRecordMapper.selectList();
        return convertToAlertVOList(records);
    }

    /**
     * 将预警记录DO列表转换为VO列表
     */
    private List<MarketAlertVO> convertToAlertVOList(List<ErpMarketAlertRecordDO> records) {
        List<MarketAlertVO> alerts = new ArrayList<>();
        for (ErpMarketAlertRecordDO record : records) {
            alerts.add(convertToAlertVO(record));
        }
        return alerts;
    }

    /**
     * 将预警记录DO转换为VO
     */
    private MarketAlertVO convertToAlertVO(ErpMarketAlertRecordDO record) {
        MarketAlertVO alert = new MarketAlertVO();
        alert.setId(record.getId());
        alert.setRuleCode(record.getRuleCode());
        alert.setRuleName(record.getRuleName());
        alert.setLevel(record.getLevel());
        alert.setProjectId(record.getProjectId());
        alert.setOrderId(record.getOrderId());
        alert.setOrderNo(record.getOrderNo());
        alert.setContent(record.getContent());
        alert.setTriggerTime(record.getTriggerTime());
        alert.setHandled(record.getHandled());
        return alert;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int checkAndTriggerAlerts() {
        // 1. 读取规则配置
        int receiptThresholdDays = DEFAULT_RECEIPT_THRESHOLD_DAYS;
        int deliveryThresholdDays = DEFAULT_DELIVERY_THRESHOLD_DAYS;
        int releaseThresholdDays = DEFAULT_RELEASE_THRESHOLD_DAYS;
        int invoiceThresholdDays = DEFAULT_INVOICE_THRESHOLD_DAYS;

        List<ErpMarketAlertRuleDO> rules = erpMarketAlertRuleMapper.selectList();
        for (ErpMarketAlertRuleDO rule : rules) {
            if (!Boolean.TRUE.equals(rule.getEnabled())) continue;
            switch (rule.getRuleCode()) {
                case "RECEIPT_OVERDUE":
                    receiptThresholdDays = rule.getThresholdDays() != null ? rule.getThresholdDays() : DEFAULT_RECEIPT_THRESHOLD_DAYS;
                    break;
                case "DELIVERY_OVERDUE":
                    deliveryThresholdDays = rule.getThresholdDays() != null ? rule.getThresholdDays() : DEFAULT_DELIVERY_THRESHOLD_DAYS;
                    break;
                case "RELEASE_BLOCKED":
                    releaseThresholdDays = rule.getThresholdDays() != null ? rule.getThresholdDays() : DEFAULT_RELEASE_THRESHOLD_DAYS;
                    break;
                case "INVOICE_OVERDUE":
                    invoiceThresholdDays = rule.getThresholdDays() != null ? rule.getThresholdDays() : DEFAULT_INVOICE_THRESHOLD_DAYS;
                    break;
            }
        }

        // 2. 查询已审核的订单（只检查活跃订单，避免全表扫描）
        // 优化：只查询状态为"已审核"的订单，排除未审核、已驳回、已结转、已作废的订单
        List<ErpSaleOrderDO> orders = erpSaleOrderMapper.selectList(
                new LambdaQueryWrapperX<ErpSaleOrderDO>()
                        .eq(ErpSaleOrderDO::getStatus, ErpAuditStatus.APPROVE.getStatus())
        );
        LocalDate today = LocalDate.now();
        int newAlertCount = 0;
        // 收集需要发送通知的预警记录
        List<ErpMarketAlertRecordDO> pendingNotifications = new ArrayList<>();

        // 批量查询所有订单的收款金额（避免 N+1 查询）
        Set<Long> orderIds = orders.stream().map(ErpSaleOrderDO::getId).collect(Collectors.toSet());
        Map<Long, BigDecimal> receivedAmountMap = erpFinanceReceiptService.getReceivedAmountByOrderIds(orderIds);

        for (ErpSaleOrderDO order : orders) {
            // 2.1 检查收款逾期
            if (order.getDeliveryDate() != null && order.getDeliveryDate().isBefore(today)) {
                BigDecimal receivedAmount = receivedAmountMap.getOrDefault(order.getId(), BigDecimal.ZERO);
                BigDecimal orderAmount = order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO;

                if (receivedAmount.compareTo(orderAmount) < 0) {
                    long daysOverdue = ChronoUnit.DAYS.between(order.getDeliveryDate(), today);
                    if (daysOverdue >= receiptThresholdDays) {
                        ErpMarketAlertRecordDO record = saveAlertRecord("RECEIPT_OVERDUE", "收款逾期预警", "WARNING",
                                order, "订单交期已过" + daysOverdue + "天，仍未收到货款");
                        if (record != null) {
                            newAlertCount++;
                            pendingNotifications.add(record);
                        }
                    }
                }
            }

            // 2.2 检查交期逾期
            if (order.getDeliveryDate() != null && order.getDeliveryDate().isBefore(today)) {
                if (order.getOutCount() == null || order.getOutCount().compareTo(BigDecimal.ZERO) == 0) {
                    long daysOverdue = ChronoUnit.DAYS.between(order.getDeliveryDate(), today);
                    if (daysOverdue >= deliveryThresholdDays) {
                        ErpMarketAlertRecordDO record = saveAlertRecord("DELIVERY_OVERDUE", "交期逾期预警", "DANGER",
                                order, "订单交期已过" + daysOverdue + "天，但未出库");
                        if (record != null) {
                            newAlertCount++;
                            pendingNotifications.add(record);
                        }
                    }
                }
            }

            // 2.3 检查放行阻塞
            if ("BLOCKED".equals(order.getShipmentReleaseStatus())) {
                String reason = order.getShipmentReleaseReason() != null ? order.getShipmentReleaseReason() : "未知原因";
                ErpMarketAlertRecordDO record = saveAlertRecord("RELEASE_BLOCKED", "放行阻塞预警", "WARNING",
                        order, "订单放行状态为阻塞：" + reason);
                if (record != null) {
                    newAlertCount++;
                    pendingNotifications.add(record);
                }
            }

            // 2.4 检查开票逾期
            if (order.getOutCount() != null && order.getOutCount().compareTo(BigDecimal.ZERO) > 0
                    && "NOT_INVOICED".equals(order.getInvoiceStatus())) {
                if (order.getUpdateTime() != null) {
                    long daysSinceOutbound = ChronoUnit.DAYS.between(
                            order.getUpdateTime().toLocalDate(), today);
                    if (daysSinceOutbound >= invoiceThresholdDays) {
                        ErpMarketAlertRecordDO record = saveAlertRecord("INVOICE_OVERDUE", "开票逾期预警", "WARNING",
                                order, "出库后已过" + daysSinceOutbound + "天，仍未开票");
                        if (record != null) {
                            newAlertCount++;
                            pendingNotifications.add(record);
                        }
                    }
                }
            }
        }

        log.info("[checkAndTriggerAlerts] 预警检查完成，新触发 {} 条预警", newAlertCount);

        // 3. 事务提交后发送通知（避免事务内调外部系统）
        // TODO: 当前实现是逐条发送通知，如果通知量大（如100+），考虑使用线程池并行发送或批量发送API
        if (!pendingNotifications.isEmpty()) {
            List<ErpMarketAlertRecordDO> notifications = new ArrayList<>(pendingNotifications);
            org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        for (ErpMarketAlertRecordDO record : notifications) {
                            try {
                                sendAlertNotification(record);
                            } catch (Exception e) {
                                log.error("[checkAndTriggerAlerts] 发送预警通知失败，recordId={}", record.getId(), e);
                            }
                        }
                    }
                });
        }

        return newAlertCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAlert(Long alertId, String handleRemark) {
        ErpMarketAlertRecordDO record = erpMarketAlertRecordMapper.selectById(alertId);
        if (record == null) {
            throw exception(MARKET_ALERT_NOT_EXISTS);
        }
        record.setHandled(true);
        record.setHandleTime(LocalDateTime.now());
        record.setHandleRemark(handleRemark);
        erpMarketAlertRecordMapper.updateById(record);
        log.info("[handleAlert] 预警已处理：{}", alertId);
    }

    /**
     * 保存预警记录（去重：同一订单同一规则同一天只记录一次）
     *
     * @return 新增的记录，如果已存在则返回 null
     */
    private ErpMarketAlertRecordDO saveAlertRecord(String ruleCode, String ruleName, String level,
                                                    ErpSaleOrderDO order, String content) {
        // 检查是否已存在同一天的同类型预警（使用 trigger_date 字段去重）
        LocalDate today = LocalDate.now();
        long existingCount = erpMarketAlertRecordMapper.selectCount(
                new LambdaQueryWrapperX<ErpMarketAlertRecordDO>()
                        .eq(ErpMarketAlertRecordDO::getRuleCode, ruleCode)
                        .eq(ErpMarketAlertRecordDO::getOrderId, order.getId())
                        .eq(ErpMarketAlertRecordDO::getTriggerDate, today)
        );
        if (existingCount > 0) {
            return null; // 已存在，不重复记录
        }

        // 保存新记录
        LocalDateTime now = LocalDateTime.now();
        ErpMarketAlertRecordDO record = ErpMarketAlertRecordDO.builder()
                .ruleCode(ruleCode)
                .ruleName(ruleName)
                .level(level)
                .projectId(order.getProjectId())
                .orderId(order.getId())
                .orderNo(order.getNo())
                .content(content)
                .triggerTime(now)
                .triggerDate(today)
                .handled(false)
                .build();
        erpMarketAlertRecordMapper.insert(record);

        return record;
    }

    /**
     * 发送预警站内信通知
     *
     * 通知销售人员
     */
    private void sendAlertNotification(ErpMarketAlertRecordDO record) {
        if (record.getOrderId() == null) {
            return;
        }

        ErpSaleOrderDO order = erpSaleOrderMapper.selectById(record.getOrderId());
        if (order == null || order.getSaleUserId() == null) {
            log.debug("[sendAlertNotification] 无销售人员，跳过通知，orderId={}", record.getOrderId());
            return;
        }

        try {
            NotifySendSingleToUserReqDTO reqDTO = new NotifySendSingleToUserReqDTO();
            reqDTO.setUserId(order.getSaleUserId());
            reqDTO.setTemplateCode("market_alert");
            reqDTO.setTemplateParams(java.util.Map.of(
                    "level", record.getLevel(),
                    "ruleName", record.getRuleName(),
                    "orderNo", record.getOrderNo() != null ? record.getOrderNo() : "-",
                    "content", record.getContent()
            ));
            notifyMessageSendApi.sendSingleMessageToAdmin(reqDTO);
            log.info("[sendAlertNotification] 预警通知已发送，userId={}, orderId={}",
                    order.getSaleUserId(), record.getOrderId());
        } catch (Exception e) {
            // 通知失败不影响主流程，记录错误日志便于排查
            log.error("[sendAlertNotification] 发送预警通知失败，recordId={}, orderId={}, error={}",
                    record.getId(), record.getOrderId(), e.getMessage(), e);
        }
    }

}
