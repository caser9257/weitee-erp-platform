package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceVoucherLogDO;

import java.util.List;

public interface ErpFinanceVoucherLogService {

    void createVoucherLog(ErpFinanceVoucherLogDO log);

    List<ErpFinanceVoucherLogDO> getVoucherLogsByVoucherId(Long voucherId);
}
