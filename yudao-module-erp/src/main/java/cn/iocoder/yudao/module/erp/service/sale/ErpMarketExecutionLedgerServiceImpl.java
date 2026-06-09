package cn.iocoder.yudao.module.erp.service.sale;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.MarketLedgerPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.MarketLedgerStatsVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.MarketLedgerVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpCustomerDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.mysql.project.ErpProjectMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

        // 3. 转换为台账 VO
        List<MarketLedgerVO> ledgerList = orderPage.getList().stream()
                .map(order -> convertToLedgerVO(order, projectMap.get(order.getProjectId())))
                .collect(Collectors.toList());

        return new PageResult<>(ledgerList, orderPage.getTotal());
    }

    @Override
    public MarketLedgerStatsVO getLedgerStats() {
        MarketLedgerStatsVO stats = new MarketLedgerStatsVO();

        // 1. 查询总订单数和总金额
        List<ErpSaleOrderDO> allOrders = erpSaleOrderMapper.selectList();
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

        // 7. 计算已收款金额（简化实现，实际需要查询收款单）
        // TODO: 需要集成收款服务查询实际收款金额
        stats.setTotalReceivedAmount(BigDecimal.ZERO);

        return stats;
    }

    @Override
    public MarketLedgerVO getProjectSummary(Long projectId) {
        // TODO: 实现项目级聚合视图
        return null;
    }

    /**
     * 转换为台账 VO
     */
    private MarketLedgerVO convertToLedgerVO(ErpSaleOrderDO order, ErpProjectDO project) {
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

        // 合同信息（需要从订单关联获取）
        vo.setContractId(order.getContractId());
        vo.setContractNo(order.getContractNo());

        // 收款信息（需要查询收款单）
        // TODO: 查询实际收款金额
        vo.setReceivedAmount(BigDecimal.ZERO);
        vo.setReceivableAmount(order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO);
        vo.setReceiptProgress(BigDecimal.ZERO);

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

        // 开票信息
        // TODO: 查询实际开票金额
        vo.setInvoicedAmount(BigDecimal.ZERO);

        return vo;
    }

}
