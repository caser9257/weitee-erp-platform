package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionManHourProjectSummaryReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionManHourProjectSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionManHourPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionManHourSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionManHourDO;

import javax.validation.Valid;
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
