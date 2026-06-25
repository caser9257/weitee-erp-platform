package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionManHourProjectSummaryReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionManHourProjectSummaryRespVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionManHourPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionManHourSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionManHourDO;

import jakarta.validation.Valid;
import java.util.List;

public interface ErpProductionManHourService {

    Long createProductionManHour(@Valid ErpProductionManHourSaveReqVO createReqVO);

    void updateProductionManHour(@Valid ErpProductionManHourSaveReqVO updateReqVO);

    void deleteProductionManHour(Long id);

    ErpProductionManHourDO getProductionManHour(Long id);

    PageResult<ErpProductionManHourDO> getProductionManHourPage(ErpProductionManHourPageReqVO pageReqVO);

    List<ErpProductionManHourProjectSummaryRespVO> getProductionManHourProjectSummaryList(
            ErpProductionManHourProjectSummaryReqVO reqVO);

}
