package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.receipt.ErpServiceReceiptPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.receipt.ErpServiceReceiptSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpServiceReceiptDO;

import java.util.List;

/**
 * 服务接收鍗?Service 接口
 *
 * @author weitee
 */
public interface ErpServiceReceiptService {

    Long createServiceReceipt(ErpServiceReceiptSaveReqVO reqVO);

    void updateServiceReceipt(ErpServiceReceiptSaveReqVO reqVO);

    void deleteServiceReceipt(Long id);

    ErpServiceReceiptDO getServiceReceipt(Long id);

    PageResult<ErpServiceReceiptDO> getServiceReceiptPage(ErpServiceReceiptPageReqVO reqVO);

    List<ErpServiceReceiptDO> getServiceReceiptList();

    /**
     * 确认服务接收鍗?
     */
    void confirmServiceReceipt(Long id);

}
