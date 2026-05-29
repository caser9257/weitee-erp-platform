package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpResultComponentDO;
import cn.iocoder.yudao.module.erp.service.mrp.support.ErpMrpNettingComponentResult;

import java.util.List;

public interface ErpMrpResultComponentService {

    void saveComponents(Long planId, Long resultId, Long materialId, List<ErpMrpNettingComponentResult> components);

    void deleteByPlanId(Long planId);

    List<ErpMrpResultComponentDO> getListByResultId(Long resultId);
}
