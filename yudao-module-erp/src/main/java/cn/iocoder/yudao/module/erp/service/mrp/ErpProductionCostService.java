package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostDetailRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostEntryPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostProjectSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostEntrySaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionCostEntryDO;

import javax.validation.Valid;
import java.util.List;

public interface ErpProductionCostService {

    Long createProductionCostEntry(@Valid ErpProductionCostEntrySaveReqVO createReqVO);

    void updateProductionCostEntry(@Valid ErpProductionCostEntrySaveReqVO updateReqVO);

    void deleteProductionCostEntry(Long id);

    ErpProductionCostEntryDO getProductionCostEntry(Long id);

    PageResult<ErpProductionCostEntryDO> getProductionCostEntryPage(ErpProductionCostEntryPageReqVO pageReqVO);

    PageResult<ErpProductionCostSummaryRespVO> getProductionCostSummaryPage(ErpProductionCostEntryPageReqVO pageReqVO);

    List<ErpProductionCostEntryDO> getProductionCostEntryListByProductionOrderId(Long productionOrderId);

    ErpProductionCostDetailRespVO getCostDetail(Long productionOrderId);

    List<ErpProductionCostProjectSummaryRespVO> getProjectSummary(String accountingMonth);

}
