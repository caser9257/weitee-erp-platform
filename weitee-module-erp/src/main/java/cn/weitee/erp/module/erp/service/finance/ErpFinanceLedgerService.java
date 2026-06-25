package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.ledger.ErpFinanceLedgerPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.ledger.ErpFinanceLedgerSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;

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
