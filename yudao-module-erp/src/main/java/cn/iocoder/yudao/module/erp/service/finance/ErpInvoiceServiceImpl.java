package cn.iocoder.yudao.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.invoice.ErpInvoicePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.invoice.ErpInvoiceSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpInvoiceDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpInvoiceItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpInvoiceItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpInvoiceMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    @Override
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
    }

    @Override
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

    /**
     * 生成发票号
     */
    private String generateInvoiceNo() {
        return noRedisDAO.generate(ErpNoRedisDAO.INVOICE_NO_PREFIX);
    }

}
