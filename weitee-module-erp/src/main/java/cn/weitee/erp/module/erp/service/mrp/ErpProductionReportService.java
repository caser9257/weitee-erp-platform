package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.report.ErpProductionReportCreateReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.report.ErpProductionReportPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionReportDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionReportItemDO;

import jakarta.validation.Valid;
import java.util.List;

public interface ErpProductionReportService {

    Long createReport(@Valid ErpProductionReportCreateReqVO reqVO);

    ErpProductionReportDO getReport(Long id);

    List<ErpProductionReportItemDO> getReportItemList(Long reportId);

    PageResult<ErpProductionReportDO> getReportPage(@Valid ErpProductionReportPageReqVO pageReqVO);

}
