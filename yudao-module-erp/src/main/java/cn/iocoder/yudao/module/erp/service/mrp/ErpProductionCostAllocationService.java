package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostAllocationDetailRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostAllocationPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostAllocationSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionCostAllocationDO;

import javax.validation.Valid;

public interface ErpProductionCostAllocationService {

    Long createProductionCostAllocation(@Valid ErpProductionCostAllocationSaveReqVO createReqVO);

    void updateProductionCostAllocation(@Valid ErpProductionCostAllocationSaveReqVO updateReqVO);

    void deleteProductionCostAllocation(Long id);

    ErpProductionCostAllocationDO getProductionCostAllocation(Long id);

    PageResult<ErpProductionCostAllocationDO> getProductionCostAllocationPage(ErpProductionCostAllocationPageReqVO pageReqVO);

    void executeProductionCostAllocation(Long id);

    ErpProductionCostAllocationDetailRespVO getProductionCostAllocationDetail(Long id);

}
