package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.period.ErpFinancePeriodCreateYearReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.period.ErpFinancePeriodPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.period.ErpFinancePeriodSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePeriodDO;

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
