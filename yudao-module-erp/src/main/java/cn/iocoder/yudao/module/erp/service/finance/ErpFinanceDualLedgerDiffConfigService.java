package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerDiffConfigPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerDiffConfigSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerDiffConfigDO;

import java.util.List;

public interface ErpFinanceDualLedgerDiffConfigService {

    Long createDualLedgerDiffConfig(ErpFinanceDualLedgerDiffConfigSaveReqVO createReqVO);

    void updateDualLedgerDiffConfig(ErpFinanceDualLedgerDiffConfigSaveReqVO updateReqVO);

    void deleteDualLedgerDiffConfig(Long id);

    ErpFinanceDualLedgerDiffConfigDO getDualLedgerDiffConfig(Long id);

    List<ErpFinanceDualLedgerDiffConfigDO> getDualLedgerDiffConfigList(Integer bizType, Integer status);

    PageResult<ErpFinanceDualLedgerDiffConfigDO> getDualLedgerDiffConfigPage(ErpFinanceDualLedgerDiffConfigPageReqVO pageReqVO);

}
