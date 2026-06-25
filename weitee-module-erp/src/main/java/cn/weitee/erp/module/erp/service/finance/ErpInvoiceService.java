package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.invoice.ErpInvoicePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.invoice.ErpInvoiceSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpInvoiceDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpInvoiceItemDO;

import jakarta.validation.Valid;
import java.util.List;

/**
 * ERP 销项发票 Service 接口
 *
 * @author system
 */
public interface ErpInvoiceService {

    /**
     * 创建销项发票
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createInvoice(@Valid ErpInvoiceSaveReqVO createReqVO);

    /**
     * 更新销项发票
     *
     * @param updateReqVO 更新信息
     */
    void updateInvoice(@Valid ErpInvoiceSaveReqVO updateReqVO);

    /**
     * 更新销项发票状态
     *
     * @param id 编号
     * @param status 状态
     */
    void updateInvoiceStatus(Long id, String status);

    /**
     * 删除销项发票
     *
     * @param ids 编号数组
     */
    void deleteInvoice(List<Long> ids);

    /**
     * 获得销项发票
     *
     * @param id 编号
     * @return 销项发票
     */
    ErpInvoiceDO getInvoice(Long id);

    /**
     * 获得销项发票分页
     *
     * @param pageReqVO 分页查询
     * @return 销项发票分页
     */
    PageResult<ErpInvoiceDO> getInvoicePage(ErpInvoicePageReqVO pageReqVO);

    /**
     * 获得销项发票明细列表
     *
     * @param invoiceId 发票编号
     * @return 销项发票明细列表
     */
    List<ErpInvoiceItemDO> getInvoiceItemListByInvoiceId(Long invoiceId);

    /**
     * 根据订单ID获取已开票金额
     *
     * @param orderId 订单编号
     * @return 已开票金额
     */
    java.math.BigDecimal getInvoicedAmountByOrderId(Long orderId);

    /**
     * 批量获取订单的已开票金额
     *
     * @param orderIds 订单编号集合
     * @return key=orderId, value=已开票金额
     */
    java.util.Map<Long, java.math.BigDecimal> getInvoicedAmountByOrderIds(java.util.Collection<Long> orderIds);

    /**
     * 获取订单可开票明细（排除已开票数量）
     *
     * @param orderId 订单编号
     * @return 可开票明细列表
     */
    List<UninvoicedItemVO> getUninvoicedItems(Long orderId);

    /**
     * 可开票明细 VO
     */
    @lombok.Data
    class UninvoicedItemVO {
        private Long productId;
        private String productName;
        private String productSpec;
        private String unit;
        private java.math.BigDecimal totalCount;     // 订单数量
        private java.math.BigDecimal invoicedCount;  // 已开票数量
        private java.math.BigDecimal availableCount; // 可开票数量
        private java.math.BigDecimal price;          // 单价
    }

}
