package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderStepDO;

import java.util.List;

public interface ErpProductionOrderStepService {

    List<ErpProductionOrderStepDO> getStepList(Long productionOrderId);

    void startStep(Long stepId);

    void pauseStep(Long stepId);

    void resumeStep(Long stepId);

    void finishStep(Long stepId);

}
