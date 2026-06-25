package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostAllocationDetailRespVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostAllocationPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostAllocationSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionCostAllocationDO;

import jakarta.validation.Valid;

public interface ErpProductionCostAllocationService {

    Long createProductionCostAllocation(@Valid ErpProductionCostAllocationSaveReqVO createReqVO);

    void updateProductionCostAllocation(@Valid ErpProductionCostAllocationSaveReqVO updateReqVO);

    void deleteProductionCostAllocation(Long id);

    ErpProductionCostAllocationDO getProductionCostAllocation(Long id);

    PageResult<ErpProductionCostAllocationDO> getProductionCostAllocationPage(ErpProductionCostAllocationPageReqVO pageReqVO);

    void executeProductionCostAllocation(Long id);

    ErpProductionCostAllocationDetailRespVO getProductionCostAllocationDetail(Long id);

}
