package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.module.erp.controller.admin.mrp.vo.returning.ErpProductionReturnCreateReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.returning.ErpProductionReturnableBatchesRespVO;

import jakarta.validation.Valid;

public interface ErpProductionReturnService {

    ErpProductionReturnableBatchesRespVO getReturnableBatches(Long productionMaterialId);

    Long createProductionReturn(@Valid ErpProductionReturnCreateReqVO reqVO);

}
