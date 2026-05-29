package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.material.ErpProductionMaterialBatchCandidatesRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.material.ErpProductionMaterialRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionMaterialDO;

import java.util.List;

public interface ErpProductionMaterialService {

    ErpProductionMaterialDO validateProductionMaterial(Long id);

    List<ErpProductionMaterialRespVO> getProductionMaterialList(Long productionOrderId);

    ErpProductionMaterialBatchCandidatesRespVO getBatchCandidates(Long productionMaterialId, Long warehouseId);

}
