package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerConfigPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerConfigSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerConfigDO;

import java.util.List;

public interface ErpFinanceDualLedgerConfigService {

    Long createDualLedgerConfig(ErpFinanceDualLedgerConfigSaveReqVO createReqVO);

    void updateDualLedgerConfig(ErpFinanceDualLedgerConfigSaveReqVO updateReqVO);

    void deleteDualLedgerConfig(Long id);

    ErpFinanceDualLedgerConfigDO getDualLedgerConfig(Long id);

    ErpFinanceDualLedgerConfigDO getEnabledDualLedgerConfig(Integer bizType);

    List<ErpFinanceDualLedgerConfigDO> getDualLedgerConfigListByStatus(Integer status);

    PageResult<ErpFinanceDualLedgerConfigDO> getDualLedgerConfigPage(ErpFinanceDualLedgerConfigPageReqVO pageReqVO);
}
