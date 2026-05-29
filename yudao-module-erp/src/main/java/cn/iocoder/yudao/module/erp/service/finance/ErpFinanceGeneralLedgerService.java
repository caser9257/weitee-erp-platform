package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.generalledger.ErpFinanceGeneralLedgerRebuildReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.generalledger.ErpFinanceGeneralLedgerRebuildRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.generalledger.ErpFinanceGeneralLedgerDetailReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.generalledger.ErpFinanceGeneralLedgerDetailRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.generalledger.ErpFinanceSubjectBalancePageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceSubjectBalanceDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceVoucherDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceVoucherEntryDO;

import javax.validation.Valid;
import java.util.List;

public interface ErpFinanceGeneralLedgerService {

    void applyPostedVoucher(ErpFinanceVoucherDO voucher, List<ErpFinanceVoucherEntryDO> entries);

    void rollbackPostedVoucher(ErpFinanceVoucherDO voucher, List<ErpFinanceVoucherEntryDO> entries);

    PageResult<ErpFinanceSubjectBalanceDO> getSubjectBalancePage(@Valid ErpFinanceSubjectBalancePageReqVO reqVO);

    ErpFinanceGeneralLedgerDetailRespVO getGeneralLedgerDetail(@Valid ErpFinanceGeneralLedgerDetailReqVO reqVO);

    ErpFinanceGeneralLedgerRebuildRespVO rebuildSubjectBalance(@Valid ErpFinanceGeneralLedgerRebuildReqVO reqVO);

}
