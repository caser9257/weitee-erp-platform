package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerDiffConfigPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerDiffConfigSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerDiffConfigDO;

import java.util.List;

public interface ErpFinanceDualLedgerDiffConfigService {

    Long createDualLedgerDiffConfig(ErpFinanceDualLedgerDiffConfigSaveReqVO createReqVO);

    void updateDualLedgerDiffConfig(ErpFinanceDualLedgerDiffConfigSaveReqVO updateReqVO);

    void deleteDualLedgerDiffConfig(Long id);

    ErpFinanceDualLedgerDiffConfigDO getDualLedgerDiffConfig(Long id);

    List<ErpFinanceDualLedgerDiffConfigDO> getDualLedgerDiffConfigList(Integer bizType, Integer status);

    PageResult<ErpFinanceDualLedgerDiffConfigDO> getDualLedgerDiffConfigPage(ErpFinanceDualLedgerDiffConfigPageReqVO pageReqVO);

}
