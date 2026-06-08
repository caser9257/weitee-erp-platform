package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoiceCancelMatchReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoiceMatchReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoicePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoicePendingItemPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoicePendingItemRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoiceSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApInvoiceDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApInvoiceMatchItemDO;

import jakarta.validation.Valid;
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

}
