package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerConfigPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerConfigSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerConfigDO;

import java.util.List;

public interface ErpFinanceDualLedgerConfigService {

    Long createDualLedgerConfig(ErpFinanceDualLedgerConfigSaveReqVO createReqVO);

    void updateDualLedgerConfig(ErpFinanceDualLedgerConfigSaveReqVO updateReqVO);

    void deleteDualLedgerConfig(Long id);

    ErpFinanceDualLedgerConfigDO getDualLedgerConfig(Long id);

    ErpFinanceDualLedgerConfigDO getEnabledDualLedgerConfig(Integer bizType);

    List<ErpFinanceDualLedgerConfigDO> getDualLedgerConfigListByStatus(Integer status);

    PageResult<ErpFinanceDualLedgerConfigDO> getDualLedgerConfigPage(ErpFinanceDualLedgerConfigPageReqVO pageReqVO);

    PageResult<ErpFinanceDualLedgerConfigDO> getDualLedgerConfigPage(
            ErpFinanceDualLedgerConfigPageReqVO pageReqVO, List<Long> visibleLedgerIds);
}
