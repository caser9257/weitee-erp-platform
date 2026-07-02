package cn.weitee.erp.module.erp.service.sale;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.MarketLedgerPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.MarketLedgerStatsVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.MarketLedgerVO;
import cn.weitee.erp.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpCustomerDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.weitee.erp.module.erp.dal.mysql.project.ErpProjectMapper;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceReceiptService;
import cn.weitee.erp.module.erp.service.finance.ErpInvoiceService;
import cn.weitee.erp.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 市场执行台账服务实现
 *
 * @author system
 */
@Service
@Validated
@Slf4j
public class ErpMarketExecutionLedgerServiceImpl implements ErpMarketExecutionLedgerService {

    @Resource
    private ErpSaleOrderMapper erpSaleOrderMapper;

    @Resource
    private ErpProjectMapper erpProjectMapper;

    @Resource
    private ErpFinanceReceiptService erpFinanceReceiptService;

    @Resource
    private ErpInvoiceService erpInvoiceService;

    @Resource
    private ErpSaleOutService erpSaleOutService;

    @Override
    public PageResult<MarketLedgerVO> getLedgerPage(MarketLedgerPageReqVO reqVO) {
        // 1. 查询销售订单
        MPJLambdaWrapperX<ErpSaleOrderDO> query = new MPJLambdaWrapperX<ErpSaleOrderDO>()
                .eqIfPresent(ErpSaleOrderDO::getSaleUserId, reqVO.getSaleUserId())
                .eqIfPresent(ErpSaleOrderDO::getContractNo, reqVO.getContractNo())
                .eqIfPresent(ErpSaleOrderDO::getShipmentReleaseStatus, reqVO.getReleaseStatus())
                .eqIfPresent(ErpSaleOrderDO::getInvoiceStatus, reqVO.getInvoiceStatus())
                .eqIfPresent(ErpSaleOrderDO::getAcceptanceStatus, reqVO.getAcceptanceStatus())
                .selectAll(ErpSaleOrderDO.class)
                .orderByDesc(ErpSaleOrderDO::getId);

        // 交期范围筛选
        if (StrUtil.isNotBlank(reqVO.getDeliveryDateStart())) {
            query.ge(ErpSaleOrderDO::getDeliveryDate, LocalDate.parse(reqVO.getDeliveryDateStart()));
        }
        if (StrUtil.isNotBlank(reqVO.getDeliveryDateEnd())) {
            query.le(ErpSaleOrderDO::getDeliveryDate, LocalDate.parse(reqVO.getDeliveryDateEnd()));
        }

        // 订单月份筛选
        if (StrUtil.isNotBlank(reqVO.getOrderMonth())) {
            YearMonth ym = YearMonth.parse(reqVO.getOrderMonth(), DateTimeFormatter.ofPattern("yyyy-MM"));
            LocalDate monthStart = ym.atDay(1);
            LocalDate monthEnd = ym.atEndOfMonth();
            query.ge(ErpSaleOrderDO::getDeliveryDate, monthStart)
                 .le(ErpSaleOrderDO::getDeliveryDate, monthEnd);
        }

        if (reqVO.getProjectNo() != null || reqVO.getLifecycleStage() != null || reqVO.getCustomerName() != null) {
            query.leftJoin(ErpProjectDO.class, ErpProjectDO::getId, ErpSaleOrderDO::getProjectId)
                    .leftJoin(ErpCustomerDO.class, ErpCustomerDO::getId, ErpSaleOrderDO::getCustomerId)
                    .eqIfPresent(ErpProjectDO::getNo, reqVO.getProjectNo())
                    .eqIfPresent(ErpProjectDO::getLifecycleStage, reqVO.getLifecycleStage())
                    .likeIfPresent(ErpCustomerDO::getName, reqVO.getCustomerName());
        }
        PageResult<ErpSaleOrderDO> orderPage = erpSaleOrderMapper.selectJoinPage(reqVO, ErpSaleOrderDO.class, query);
        if (CollUtil.isEmpty(orderPage.getList())) {
            return new PageResult<>(new ArrayList<>(), orderPage.getTotal());
        }

        // 2. 获取项目信息
        Set<Long> projectIds = orderPage.getList().stream()
                .map(ErpSaleOrderDO::getProjectId)
                .filter(pid -> pid != null)
                .collect(Collectors.toSet());
        Map<Long, ErpProjectDO> projectMap = erpProjectMapper.selectBatchIds(projectIds).stream()
                .collect(Collectors.toMap(ErpProjectDO::getId, p -> p));

        // 3. 批量查询收款和开票金额（避免 N+1）
        Set<Long> orderIds = orderPage.getList().stream()
                .map(ErpSaleOrderDO::getId)
                .collect(Collectors.toSet());
        Map<Long, BigDecimal> receivedAmountMap = erpFinanceReceiptService.getReceivedAmountByOrderIds(orderIds);
        Map<Long, BigDecimal> invoicedAmountMap = erpInvoiceService.getInvoicedAmountByOrderIds(orderIds);

        // 4. 转换为台账 VO
        List<MarketLedgerVO> ledgerList = orderPage.getList().stream()
                .map(order -> convertToLedgerVO(order, projectMap.get(order.getProjectId()),
                        receivedAmountMap.getOrDefault(order.getId(), BigDecimal.ZERO),
                        invoicedAmountMap.getOrDefault(order.getId(), BigDecimal.ZERO)))
                .collect(Collectors.toList());

        return new PageResult<>(ledgerList, orderPage.getTotal());
    }

    @Override
    public MarketLedgerStatsVO getLedgerStats() {
        MarketLedgerStatsVO stats = new MarketLedgerStatsVO();

        // 1. 查询总订单数和总金额（仅加载统计所需的精简列，避免全列全表扫描）
        List<ErpSaleOrderDO> allOrders = erpSaleOrderMapper.selectListForMarketLedgerStats();
        stats.setTotalOrderCount(allOrders.size());
        BigDecimal totalAmount = allOrders.stream()
                .map(ErpSaleOrderDO::getTotalPrice)
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalOrderAmount(totalAmount);

        // 2. 查询待放行订单数
        long pendingReleaseCount = allOrders.stream()
                .filter(order -> "PENDING".equals(order.getShipmentReleaseStatus()) || 
                                "BLOCKED".equals(order.getShipmentReleaseStatus()))
                .count();
        stats.setPendingReleaseCount((int) pendingReleaseCount);

        // 3. 查询已放行订单数
        long releasedCount = allOrders.stream()
                .filter(order -> "RELEASED".equals(order.getShipmentReleaseStatus()))
                .count();
        stats.setShippedOrderCount((int) releasedCount);

        // 4. 查询待开票订单数
        long pendingInvoiceCount = allOrders.stream()
                .filter(order -> "NOT_INVOICED".equals(order.getInvoiceStatus()))
                .count();
        stats.setPendingInvoiceCount((int) pendingInvoiceCount);

        // 5. 查询待验收订单数
        long pendingAcceptanceCount = allOrders.stream()
                .filter(order -> "PENDING".equals(order.getAcceptanceStatus()))
                .count();
        stats.setPendingAcceptanceCount((int) pendingAcceptanceCount);

        // 6. 查询异常订单数（阻塞状态）
        long abnormalCount = allOrders.stream()
                .filter(order -> "BLOCKED".equals(order.getShipmentReleaseStatus()))
                .count();
        stats.setAbnormalOrderCount((int) abnormalCount);

        // 7. 汇总已收款金额（DB 层 SUM，避免把所有订单的出库单载入内存）
        BigDecimal totalReceivedAmount = erpSaleOutService.getTotalReceivedAmount();
        stats.setTotalReceivedAmount(totalReceivedAmount);

        return stats;
    }

    @Override
    public MarketLedgerVO getProjectSummary(Long projectId) {
        // 查询项目信息
        ErpProjectDO project = erpProjectMapper.selectById(projectId);
        if (project == null) {
            return null;
        }

        // 查询该项目的所有订单
        List<ErpSaleOrderDO> orders = erpSaleOrderMapper.selectList(
                ErpSaleOrderDO::getProjectId, projectId);
        if (orders.isEmpty()) {
            return null;
        }

        // 聚合计算
        MarketLedgerVO vo = new MarketLedgerVO();
        vo.setProjectId(project.getId());
        vo.setProjectNo(project.getNo());
        vo.setProjectName(project.getName());
        vo.setLifecycleStage(project.getLifecycleStage());

        // 订单汇总
        BigDecimal totalOrderAmount = orders.stream()
                .map(o -> o.getTotalPrice() != null ? o.getTotalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setOrderTotalPrice(totalOrderAmount);

        // 批量查询收款和开票金额（避免 N+1）
        Set<Long> orderIds = orders.stream()
                .map(ErpSaleOrderDO::getId)
                .collect(Collectors.toSet());
        Map<Long, BigDecimal> receivedMap = erpFinanceReceiptService.getReceivedAmountByOrderIds(orderIds);
        Map<Long, BigDecimal> invoicedMap = erpInvoiceService.getInvoicedAmountByOrderIds(orderIds);

        BigDecimal totalReceived = receivedMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setReceivedAmount(totalReceived);
        vo.setReceivableAmount(totalOrderAmount);
        if (totalOrderAmount.compareTo(BigDecimal.ZERO) > 0) {
            vo.setReceiptProgress(totalReceived
                    .multiply(new BigDecimal("100"))
                    .divide(totalOrderAmount, 2, RoundingMode.HALF_UP));
        }

        BigDecimal totalInvoiced = invoicedMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setInvoicedAmount(totalInvoiced);

        return vo;
    }

    /**
     * 转换为台账 VO
     */
    private MarketLedgerVO convertToLedgerVO(ErpSaleOrderDO order, ErpProjectDO project,
                                              BigDecimal receivedAmount, BigDecimal invoicedAmount) {
        MarketLedgerVO vo = new MarketLedgerVO();

        // 项目信息
        if (project != null) {
            vo.setProjectId(project.getId());
            vo.setProjectNo(project.getNo());
            vo.setProjectName(project.getName());
            vo.setLifecycleStage(project.getLifecycleStage());
            vo.setCurrentBlocker(project.getCurrentBlocker());
            vo.setCurrentPendingRole(project.getCurrentPendingRole());
        }

        // 订单信息
        vo.setOrderId(order.getId());
        vo.setOrderNo(order.getNo());
        vo.setOrderStatus(order.getStatus());
        vo.setOrderTotalPrice(order.getTotalPrice());
        vo.setDeliveryDate(order.getDeliveryDate());
        vo.setShipmentReleaseStatus(order.getShipmentReleaseStatus());
        vo.setShipmentReleaseReason(order.getShipmentReleaseReason());
        vo.setInvoiceStatus(order.getInvoiceStatus());
        vo.setAcceptanceStatus(order.getAcceptanceStatus());

        // 合同信息
        vo.setContractId(order.getContractId());
        vo.setContractNo(order.getContractNo());

        // 收款信息（使用预查询的金额）
        vo.setReceivedAmount(receivedAmount != null ? receivedAmount : BigDecimal.ZERO);
        vo.setReceivableAmount(order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO);
        if (vo.getReceivableAmount().compareTo(BigDecimal.ZERO) > 0) {
            vo.setReceiptProgress(vo.getReceivedAmount()
                    .multiply(new BigDecimal("100"))
                    .divide(vo.getReceivableAmount(), 2, RoundingMode.HALF_UP));
        } else {
            vo.setReceiptProgress(BigDecimal.ZERO);
        }

        // 出库信息
        vo.setShippedCount(order.getOutCount() != null ? order.getOutCount() : BigDecimal.ZERO);
        vo.setOrderCount(order.getTotalCount() != null ? order.getTotalCount() : BigDecimal.ZERO);
        if (vo.getOrderCount().compareTo(BigDecimal.ZERO) > 0) {
            vo.setShipmentProgress(vo.getShippedCount()
                    .multiply(new BigDecimal("100"))
                    .divide(vo.getOrderCount(), 2, RoundingMode.HALF_UP));
        } else {
            vo.setShipmentProgress(BigDecimal.ZERO);
        }

        // 开票信息（使用预查询的金额）
        vo.setInvoicedAmount(invoicedAmount != null ? invoicedAmount : BigDecimal.ZERO);

        return vo;
    }

}
