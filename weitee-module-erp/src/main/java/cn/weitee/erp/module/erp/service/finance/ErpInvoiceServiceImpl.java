package cn.weitee.erp.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.invoice.ErpInvoicePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.invoice.ErpInvoiceSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpInvoiceDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpInvoiceItemDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpInvoiceItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpInvoiceMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductUnitDO;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.product.ErpProductUnitService;
import cn.weitee.erp.module.erp.service.project.ErpProjectLifecycleService;
import cn.weitee.erp.module.erp.service.project.event.ProjectLifecycleRefreshEvent;
import cn.weitee.erp.module.erp.util.ErpTransactionUtils;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderItemDO;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleOrderItemMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * ERP 销项发票 Service 实现
 *
 * @author system
 */
@Service
@Validated
@Slf4j
public class ErpInvoiceServiceImpl implements ErpInvoiceService {

    @Resource
    private ErpInvoiceMapper erpInvoiceMapper;

    @Resource
    private ErpInvoiceItemMapper erpInvoiceItemMapper;

    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Resource
    private ErpProjectLifecycleService projectLifecycleService;
    @Resource
    private ApplicationEventPublisher eventPublisher;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpProductUnitService productUnitService;
    @Resource
    private ErpSaleOrderMapper erpSaleOrderMapper;
    @Resource
    private ErpSaleOrderItemMapper erpSaleOrderItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createInvoice(ErpInvoiceSaveReqVO createReqVO) {
        // 1. 创建发票
        ErpInvoiceDO invoice = new ErpInvoiceDO();
        invoice.setCustomerId(createReqVO.getCustomerId());
        invoice.setOrderId(createReqVO.getOrderId());
        invoice.setInvoiceType(createReqVO.getInvoiceType());
        invoice.setInvoiceTitle(createReqVO.getInvoiceTitle());
        invoice.setTaxpayerNo(createReqVO.getTaxpayerNo());
        invoice.setInvoiceTime(createReqVO.getInvoiceTime() != null ? createReqVO.getInvoiceTime() : LocalDateTime.now());
        invoice.setRemark(createReqVO.getRemark());
        invoice.setStatus("DRAFT");
        invoice.setNo(generateInvoiceNo());

        // 2. 计算金额
        BigDecimal totalAmountWithoutTax = BigDecimal.ZERO;
        BigDecimal totalTaxAmount = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 3. 保存发票
        erpInvoiceMapper.insert(invoice);

        // 4. 保存发票明细
        if (CollUtil.isNotEmpty(createReqVO.getItems())) {
            for (ErpInvoiceSaveReqVO.ErpInvoiceItemSaveReqVO itemReq : createReqVO.getItems()) {
                ErpInvoiceItemDO item = new ErpInvoiceItemDO();
                item.setInvoiceId(invoice.getId());
                item.setProductId(itemReq.getProductId());
                item.setProductName(itemReq.getProductName());
                item.setProductSpec(itemReq.getProductSpec());
                item.setUnit(itemReq.getUnit());
                item.setCount(itemReq.getCount());
                item.setPrice(itemReq.getPrice());
                item.setTaxRate(itemReq.getTaxRate() != null ? itemReq.getTaxRate() : BigDecimal.ZERO);

                // 计算金额
                BigDecimal amount = item.getPrice().multiply(item.getCount());
                BigDecimal taxAmount = amount.multiply(item.getTaxRate()).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
                BigDecimal itemTotalAmount = amount.add(taxAmount);

                item.setAmount(amount);
                item.setTaxAmount(taxAmount);
                item.setTotalAmount(itemTotalAmount);
                item.setRemark(itemReq.getRemark());

                erpInvoiceItemMapper.insert(item);

                totalAmountWithoutTax = totalAmountWithoutTax.add(amount);
                totalTaxAmount = totalTaxAmount.add(taxAmount);
                totalAmount = totalAmount.add(itemTotalAmount);
            }
        }

        // 5. 更新发票总金额
        invoice.setAmountWithoutTax(totalAmountWithoutTax);
        invoice.setTaxAmount(totalTaxAmount);
        invoice.setTotalAmount(totalAmount);
        erpInvoiceMapper.updateById(invoice);

        log.info("创建销项发票：{}", invoice.getId());
        return invoice.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInvoice(ErpInvoiceSaveReqVO updateReqVO) {
        // 1. 校验发票是否存在
        ErpInvoiceDO invoice = erpInvoiceMapper.selectById(updateReqVO.getId());
        if (invoice == null) {
            throw new RuntimeException("发票不存在");
        }

        // 2. 校验发票状态（只有草稿状态才能更新）
        if (!"DRAFT".equals(invoice.getStatus())) {
            throw new RuntimeException("只有草稿状态的发票才能更新");
        }

        // 3. 更新发票基本信息
        invoice.setCustomerId(updateReqVO.getCustomerId());
        invoice.setOrderId(updateReqVO.getOrderId());
        invoice.setInvoiceType(updateReqVO.getInvoiceType());
        invoice.setInvoiceTitle(updateReqVO.getInvoiceTitle());
        invoice.setTaxpayerNo(updateReqVO.getTaxpayerNo());
        invoice.setInvoiceTime(updateReqVO.getInvoiceTime());
        invoice.setRemark(updateReqVO.getRemark());

        // 4. 删除原有明细
        erpInvoiceItemMapper.delete(ErpInvoiceItemDO::getInvoiceId, invoice.getId());

        // 5. 重新计算金额并保存新明细
        BigDecimal totalAmountWithoutTax = BigDecimal.ZERO;
        BigDecimal totalTaxAmount = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (CollUtil.isNotEmpty(updateReqVO.getItems())) {
            for (ErpInvoiceSaveReqVO.ErpInvoiceItemSaveReqVO itemReq : updateReqVO.getItems()) {
                ErpInvoiceItemDO item = new ErpInvoiceItemDO();
                item.setInvoiceId(invoice.getId());
                item.setProductId(itemReq.getProductId());
                item.setProductName(itemReq.getProductName());
                item.setProductSpec(itemReq.getProductSpec());
                item.setUnit(itemReq.getUnit());
                item.setCount(itemReq.getCount());
                item.setPrice(itemReq.getPrice());
                item.setTaxRate(itemReq.getTaxRate() != null ? itemReq.getTaxRate() : BigDecimal.ZERO);

                // 计算金额
                BigDecimal amount = item.getPrice().multiply(item.getCount());
                BigDecimal taxAmount = amount.multiply(item.getTaxRate()).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
                BigDecimal itemTotalAmount = amount.add(taxAmount);

                item.setAmount(amount);
                item.setTaxAmount(taxAmount);
                item.setTotalAmount(itemTotalAmount);
                item.setRemark(itemReq.getRemark());

                erpInvoiceItemMapper.insert(item);

                totalAmountWithoutTax = totalAmountWithoutTax.add(amount);
                totalTaxAmount = totalTaxAmount.add(taxAmount);
                totalAmount = totalAmount.add(itemTotalAmount);
            }
        }

        // 6. 更新发票总金额
        invoice.setAmountWithoutTax(totalAmountWithoutTax);
        invoice.setTaxAmount(totalTaxAmount);
        invoice.setTotalAmount(totalAmount);
        erpInvoiceMapper.updateById(invoice);

        log.info("更新销项发票：{}", invoice.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInvoiceStatus(Long id, String status) {
        ErpInvoiceDO invoice = erpInvoiceMapper.selectById(id);
        if (invoice == null) {
            throw new RuntimeException("发票不存在");
        }

        ErpInvoiceDO updateInvoice = new ErpInvoiceDO();
        updateInvoice.setId(id);
        updateInvoice.setStatus(status);
        erpInvoiceMapper.updateById(updateInvoice);

        log.info("更新销项发票状态：{} -> {}", id, status);

        // 开票完成时，事务提交后异步触发项目生命周期刷新
        if ("ISSUED".equals(status) && invoice.getOrderId() != null) {
            Long orderId = invoice.getOrderId();
            ErpTransactionUtils.afterCommit(() -> {
                try {
                    ErpSaleOrderDO order = erpSaleOrderMapper.selectById(orderId);
                    if (order != null && order.getProjectId() != null) {
                        eventPublisher.publishEvent(new ProjectLifecycleRefreshEvent(
                                order.getProjectId(), "开票完成"));
                    }
                } catch (Exception e) {
                    log.warn("[updateInvoiceStatus] 触发项目生命周期刷新事件失败，orderId={}", orderId, e);
                }
            });
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInvoice(List<Long> ids) {
        // 删除发票明细
        for (Long id : ids) {
            erpInvoiceItemMapper.delete(ErpInvoiceItemDO::getInvoiceId, id);
        }
        // 删除发票
        erpInvoiceMapper.deleteByIds(ids);
        log.info("删除销项发票：{}", ids);
    }

    @Override
    public ErpInvoiceDO getInvoice(Long id) {
        return erpInvoiceMapper.selectById(id);
    }

    @Override
    public PageResult<ErpInvoiceDO> getInvoicePage(ErpInvoicePageReqVO pageReqVO) {
        return erpInvoiceMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpInvoiceItemDO> getInvoiceItemListByInvoiceId(Long invoiceId) {
        return erpInvoiceItemMapper.selectListByInvoiceId(invoiceId);
    }

    @Override
    public BigDecimal getInvoicedAmountByOrderId(Long orderId) {
        // 查询该订单下所有已开票金额
        List<ErpInvoiceDO> invoices = erpInvoiceMapper.selectList(
                ErpInvoiceDO::getOrderId, orderId,
                ErpInvoiceDO::getStatus, "ISSUED"
        );
        return invoices.stream()
                .map(ErpInvoiceDO::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Map<Long, BigDecimal> getInvoicedAmountByOrderIds(Collection<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyMap();
        }
        // 一次查询所有相关发票（使用 LambdaQueryWrapperX 支持 in + eq 组合）
        List<ErpInvoiceDO> invoices = erpInvoiceMapper.selectList(
                new cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX<ErpInvoiceDO>()
                        .in(ErpInvoiceDO::getOrderId, orderIds)
                        .eq(ErpInvoiceDO::getStatus, "ISSUED")
        );
        // 按 orderId 分组汇总
        Map<Long, BigDecimal> result = new HashMap<>();
        for (ErpInvoiceDO invoice : invoices) {
            result.merge(invoice.getOrderId(),
                    invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO,
                    BigDecimal::add);
        }
        // 确保所有 orderId 都有值
        for (Long orderId : orderIds) {
            result.putIfAbsent(orderId, BigDecimal.ZERO);
        }
        return result;
    }

    @Override
    public List<UninvoicedItemVO> getUninvoicedItems(Long orderId) {
        // 1. 获取订单产品明细
        List<ErpSaleOrderItemDO> orderItems = erpSaleOrderItemMapper.selectListByOrderId(orderId);
        if (orderItems.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 获取该订单所有已开票明细（状态为 ISSUED）
        List<ErpInvoiceDO> invoices = erpInvoiceMapper.selectList(
                ErpInvoiceDO::getOrderId, orderId,
                ErpInvoiceDO::getStatus, "ISSUED"
        );
        List<Long> invoiceIds = invoices.stream().map(ErpInvoiceDO::getId).toList();

        // 统计每个产品的已开票数量
        Map<Long, BigDecimal> invoicedCountMap = new HashMap<>();
        if (!invoiceIds.isEmpty()) {
            List<ErpInvoiceItemDO> allInvoiceItems = erpInvoiceItemMapper.selectListByInvoiceIds(invoiceIds);
            for (ErpInvoiceItemDO item : allInvoiceItems) {
                invoicedCountMap.merge(item.getProductId(), item.getCount(), BigDecimal::add);
            }
        }

        // 3. 批量获取产品信息
        Set<Long> productIds = orderItems.stream()
                .map(ErpSaleOrderItemDO::getProductId)
                .collect(java.util.stream.Collectors.toSet());
        Map<Long, ErpProductDO> productMap = productService.validProductList(productIds).stream()
                .collect(java.util.stream.Collectors.toMap(ErpProductDO::getId, p -> p));
        // 批量获取产品单位信息
        Set<Long> unitIds = productMap.values().stream()
                .map(ErpProductDO::getUnitId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        Map<Long, ErpProductUnitDO> unitMap = unitIds.isEmpty() ? Collections.emptyMap() :
                productUnitService.getProductUnitMap(unitIds);

        // 4. 计算可开票数量
        List<UninvoicedItemVO> result = new ArrayList<>();
        for (ErpSaleOrderItemDO orderItem : orderItems) {
            BigDecimal totalCount = orderItem.getCount() != null ? orderItem.getCount() : BigDecimal.ZERO;
            BigDecimal invoicedCount = invoicedCountMap.getOrDefault(orderItem.getProductId(), BigDecimal.ZERO);
            BigDecimal availableCount = totalCount.subtract(invoicedCount);

            if (availableCount.compareTo(BigDecimal.ZERO) > 0) {
                UninvoicedItemVO vo = new UninvoicedItemVO();
                vo.setProductId(orderItem.getProductId());
                // 从产品表获取产品信息
                ErpProductDO product = productMap.get(orderItem.getProductId());
                if (product != null) {
                    vo.setProductName(product.getName());
                    vo.setProductSpec(product.getBarCode());
                    // 获取单位名称
                    ErpProductUnitDO unit = product.getUnitId() != null ? unitMap.get(product.getUnitId()) : null;
                    if (unit != null) {
                        vo.setUnit(unit.getName());
                    }
                }
                vo.setTotalCount(totalCount);
                vo.setInvoicedCount(invoicedCount);
                vo.setAvailableCount(availableCount);
                vo.setPrice(orderItem.getProductPrice());
                result.add(vo);
            }
        }

        return result;
    }

    /**
     * 生成发票号
     */
    private String generateInvoiceNo() {
        return noRedisDAO.generate(ErpNoRedisDAO.INVOICE_NO_PREFIX);
    }

}
