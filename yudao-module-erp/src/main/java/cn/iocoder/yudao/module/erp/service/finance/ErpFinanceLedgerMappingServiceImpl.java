package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceLedgerMappingDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceLedgerMappingMapper;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ErpFinanceLedgerMappingServiceImpl implements ErpFinanceLedgerMappingService {
    
    @Resource
    private ErpFinanceLedgerMappingMapper ledgerMappingMapper;
    
    @Override
    public Long createLedgerMapping(ErpFinanceLedgerMappingDO mapping) {
        ledgerMappingMapper.insert(mapping);
        return mapping.getId();
    }
    
    @Override
    public void updateLedgerMapping(ErpFinanceLedgerMappingDO mapping) {
        ledgerMappingMapper.updateById(mapping);
    }
    
    @Override
    public void deleteLedgerMapping(Long id) {
        ledgerMappingMapper.deleteById(id);
    }
    
    @Override
    public ErpFinanceLedgerMappingDO getLedgerMapping(Long id) {
        return ledgerMappingMapper.selectById(id);
    }
    
    @Override
    public List<ErpFinanceLedgerMappingDO> getLedgerMappingsByExternalLedger(Long externalLedgerId) {
        return ledgerMappingMapper.selectList(
                new LambdaQueryWrapperX<ErpFinanceLedgerMappingDO>()
                        .eq(ErpFinanceLedgerMappingDO::getExternalLedgerId, externalLedgerId));
    }
    
    @Override
    public ErpFinanceLedgerMappingDO getLedgerMappingByInternalLedger(Long internalLedgerId) {
        return ledgerMappingMapper.selectOne(
                new LambdaQueryWrapperX<ErpFinanceLedgerMappingDO>()
                        .eq(ErpFinanceLedgerMappingDO::getInternalLedgerId, internalLedgerId));
    }
}