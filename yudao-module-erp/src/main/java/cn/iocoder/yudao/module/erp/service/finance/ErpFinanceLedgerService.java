package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.ledger.ErpFinanceLedgerPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.ledger.ErpFinanceLedgerSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

public interface ErpFinanceLedgerService {

    Long createFinanceLedger(ErpFinanceLedgerSaveReqVO createReqVO);

    void updateFinanceLedger(ErpFinanceLedgerSaveReqVO updateReqVO);

    void updateFinanceLedgerDefaultStatus(Long id, Boolean defaultStatus);

    void deleteFinanceLedger(Long id);

    ErpFinanceLedgerDO getFinanceLedger(Long id);

    ErpFinanceLedgerDO validateFinanceLedger(Long id);

    ErpFinanceLedgerDO getDefaultFinanceLedger();

    List<ErpFinanceLedgerDO> getFinanceLedgerListByStatus(Integer status);

    List<ErpFinanceLedgerDO> getFinanceLedgerList(Collection<Long> ids);

    PageResult<ErpFinanceLedgerDO> getFinanceLedgerPage(ErpFinanceLedgerPageReqVO pageReqVO);

    default Map<Long, ErpFinanceLedgerDO> getFinanceLedgerMap(Collection<Long> ids) {
        return convertMap(getFinanceLedgerList(ids), ErpFinanceLedgerDO::getId);
    }
}
