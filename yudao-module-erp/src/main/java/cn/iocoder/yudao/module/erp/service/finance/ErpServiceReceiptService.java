package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.receipt.ErpServiceReceiptPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.receipt.ErpServiceReceiptSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpServiceReceiptDO;

import java.util.List;

/**
 * 服务接收单 Service 接口
 *
 * @author ruoyi-vue-pro
 */
public interface ErpServiceReceiptService {

    Long createServiceReceipt(ErpServiceReceiptSaveReqVO reqVO);

    void updateServiceReceipt(ErpServiceReceiptSaveReqVO reqVO);

    void deleteServiceReceipt(Long id);

    ErpServiceReceiptDO getServiceReceipt(Long id);

    PageResult<ErpServiceReceiptDO> getServiceReceiptPage(ErpServiceReceiptPageReqVO reqVO);

    List<ErpServiceReceiptDO> getServiceReceiptList();

    /**
     * 确认服务接收单
     */
    void confirmServiceReceipt(Long id);

}
