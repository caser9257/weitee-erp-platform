package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherLogDO;

import java.util.List;

public interface ErpFinanceVoucherLogService {

    void createVoucherLog(ErpFinanceVoucherLogDO log);

    List<ErpFinanceVoucherLogDO> getVoucherLogsByVoucherId(Long voucherId);
}
