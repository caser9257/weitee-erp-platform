package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.generalledger.ErpFinanceGeneralLedgerRebuildReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.generalledger.ErpFinanceGeneralLedgerRebuildRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.generalledger.ErpFinanceGeneralLedgerDetailReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.generalledger.ErpFinanceGeneralLedgerDetailRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.generalledger.ErpFinanceSubjectBalancePageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceSubjectBalanceDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherEntryDO;

import jakarta.validation.Valid;
import java.util.List;

public interface ErpFinanceGeneralLedgerService {

    void applyPostedVoucher(ErpFinanceVoucherDO voucher, List<ErpFinanceVoucherEntryDO> entries);

    void rollbackPostedVoucher(ErpFinanceVoucherDO voucher, List<ErpFinanceVoucherEntryDO> entries);

    PageResult<ErpFinanceSubjectBalanceDO> getSubjectBalancePage(@Valid ErpFinanceSubjectBalancePageReqVO reqVO);

    ErpFinanceGeneralLedgerDetailRespVO getGeneralLedgerDetail(@Valid ErpFinanceGeneralLedgerDetailReqVO reqVO);

    ErpFinanceGeneralLedgerRebuildRespVO rebuildSubjectBalance(@Valid ErpFinanceGeneralLedgerRebuildReqVO reqVO);

}
