package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.reportitem.ErpFinanceReportItemPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.reportitem.ErpFinanceReportItemSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.reportitem.ErpFinanceReportTemplateInitReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.reportitem.ErpFinanceReportTemplateInitRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceReportItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceReportItemSubjectDO;

import java.util.Collection;
import java.util.List;

public interface ErpFinanceReportItemService {

    Long createFinanceReportItem(ErpFinanceReportItemSaveReqVO createReqVO);

    void updateFinanceReportItem(ErpFinanceReportItemSaveReqVO updateReqVO);

    void deleteFinanceReportItem(Long id);

    ErpFinanceReportItemDO getFinanceReportItem(Long id);

    PageResult<ErpFinanceReportItemDO> getFinanceReportItemPage(ErpFinanceReportItemPageReqVO pageReqVO);

    List<ErpFinanceReportItemSubjectDO> getFinanceReportItemSubjectListByItemId(Long itemId);

    List<ErpFinanceReportItemSubjectDO> getFinanceReportItemSubjectListByItemIds(Collection<Long> itemIds);

    ErpFinanceReportTemplateInitRespVO initStandardTemplate(ErpFinanceReportTemplateInitReqVO reqVO);

}
