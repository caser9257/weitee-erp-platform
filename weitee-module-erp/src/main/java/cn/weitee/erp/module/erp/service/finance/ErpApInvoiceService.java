package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoiceCancelMatchReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoiceMatchReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoicePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoicePendingItemPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoicePendingItemRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoiceSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApInvoiceDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApInvoiceMatchItemDO;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface ErpApInvoiceService {

    Long createApInvoice(@Valid ErpApInvoiceSaveReqVO createReqVO);

    void updateApInvoice(@Valid ErpApInvoiceSaveReqVO updateReqVO);

    ErpApInvoiceDO getApInvoice(Long id);

    PageResult<ErpApInvoiceDO> getApInvoicePage(ErpApInvoicePageReqVO pageReqVO);

    List<ErpApInvoiceMatchItemDO> getApInvoiceMatchItemListByInvoiceId(Long invoiceId);

    List<ErpApInvoiceMatchItemDO> getActiveMatchItemListByPurchaseInItemIds(Collection<Long> purchaseInItemIds);

    PageResult<ErpApInvoicePendingItemRespVO> getPendingItemPage(ErpApInvoicePendingItemPageReqVO pageReqVO);

    void confirmMatch(@Valid ErpApInvoiceMatchReqVO reqVO);

    void cancelMatch(@Valid ErpApInvoiceCancelMatchReqVO reqVO);

    /**
     * 根据发票号获取发票金额
     *
     * @param invoiceNo 发票号
     * @return 发票金额，如果发票不存在返回null
     */
    BigDecimal getAmountByInvoiceNo(String invoiceNo);

}
