package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerMappingDO;

import java.util.List;

public interface ErpFinanceLedgerMappingService {
    
    Long createLedgerMapping(ErpFinanceLedgerMappingDO mapping);
    
    void updateLedgerMapping(ErpFinanceLedgerMappingDO mapping);
    
    void deleteLedgerMapping(Long id);
    
    ErpFinanceLedgerMappingDO getLedgerMapping(Long id);
    
    List<ErpFinanceLedgerMappingDO> getLedgerMappingsByExternalLedger(Long externalLedgerId);
    
    ErpFinanceLedgerMappingDO getLedgerMappingByInternalLedger(Long internalLedgerId);
}