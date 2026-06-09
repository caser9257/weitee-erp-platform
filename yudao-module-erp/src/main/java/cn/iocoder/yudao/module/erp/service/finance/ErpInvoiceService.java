package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.invoice.ErpInvoicePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.invoice.ErpInvoiceSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpInvoiceDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpInvoiceItemDO;

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

}
