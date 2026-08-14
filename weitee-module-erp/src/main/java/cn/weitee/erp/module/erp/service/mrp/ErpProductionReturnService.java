package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.module.erp.controller.admin.mrp.vo.returning.ErpProductionReturnCreateReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.returning.ErpProductionReturnPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.returning.ErpProductionReturnableBatchesRespVO;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionReturnDO;

import jakarta.validation.Valid;

public interface ErpProductionReturnService {

    ErpProductionReturnableBatchesRespVO getReturnableBatches(Long productionMaterialId);

    PageResult<ErpProductionReturnDO> getProductionReturnPage(ErpProductionReturnPageReqVO pageReqVO);

    Long createProductionReturn(@Valid ErpProductionReturnCreateReqVO reqVO);

}
