package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.period.ErpFinancePeriodCreateYearReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.period.ErpFinancePeriodPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.period.ErpFinancePeriodSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePeriodDO;

import java.time.LocalDate;

public interface ErpFinancePeriodService {

    Long createFinancePeriod(ErpFinancePeriodSaveReqVO createReqVO);

    Integer createFinancePeriodsByYear(ErpFinancePeriodCreateYearReqVO createReqVO);

    void closeFinancePeriod(Long id);

    void reopenFinancePeriod(Long id);

    ErpFinancePeriodDO getFinancePeriod(Long id);

    PageResult<ErpFinancePeriodDO> getFinancePeriodPage(ErpFinancePeriodPageReqVO pageReqVO);

    ErpFinancePeriodDO getCurrentOpenPeriod(Long ledgerId, LocalDate bizDate);
}
