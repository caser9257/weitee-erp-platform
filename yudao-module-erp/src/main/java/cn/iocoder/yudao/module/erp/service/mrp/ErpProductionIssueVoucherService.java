package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.issuevoucher.ErpProductionIssueVoucherPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionIssueDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionIssueItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionIssueVoucherDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionIssueVoucherItemDO;

import java.util.Collection;
import java.util.List;

public interface ErpProductionIssueVoucherService {

    Long createVoucher(ErpProductionIssueDO issue, List<ErpProductionIssueItemDO> items);

    ErpProductionIssueVoucherDO getVoucher(Long id);

    ErpProductionIssueVoucherDO getVoucherByIssueId(Long issueId);

    PageResult<ErpProductionIssueVoucherDO> getVoucherPage(ErpProductionIssueVoucherPageReqVO pageReqVO);

    List<ErpProductionIssueVoucherItemDO> getVoucherItemListByVoucherId(Long voucherId);

    List<ErpProductionIssueVoucherItemDO> getVoucherItemListByVoucherIds(Collection<Long> voucherIds);

}
