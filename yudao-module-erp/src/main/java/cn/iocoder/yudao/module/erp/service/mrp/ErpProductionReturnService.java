package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.returning.ErpProductionReturnCreateReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.returning.ErpProductionReturnableBatchesRespVO;

import jakarta.validation.Valid;

public interface ErpProductionReturnService {

    ErpProductionReturnableBatchesRespVO getReturnableBatches(Long productionMaterialId);

    Long createProductionReturn(@Valid ErpProductionReturnCreateReqVO reqVO);

}
