package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpResultComponentDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpMrpResultComponentMapper;
import cn.iocoder.yudao.module.erp.service.mrp.support.ErpMrpNettingComponentResult;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Service
public class ErpMrpResultComponentServiceImpl implements ErpMrpResultComponentService {

    @Resource
    private ErpMrpResultComponentMapper erpMrpResultComponentMapper;

    @Override
    public void saveComponents(Long planId, Long resultId, Long materialId, List<ErpMrpNettingComponentResult> components) {
        if (components == null || components.isEmpty()) {
            return;
        }
        for (ErpMrpNettingComponentResult component : components) {
            erpMrpResultComponentMapper.insert(ErpMrpResultComponentDO.builder()
                    .planId(planId)
                    .resultId(resultId)
                    .materialId(materialId)
                    .componentCode(component.getComponentCode())
                    .componentName(component.getComponentName())
                    .componentRole(component.getComponentRole())
                    .sequenceNo(component.getSequenceNo())
                    .enableFlag(component.getEnableFlag())
                    .baseQty(component.getBaseQty())
                    .consumedQty(component.getConsumedQty())
                    .remainingQty(component.getRemainingQty())
                    .build());
        }
    }

    @Override
    public void deleteByPlanId(Long planId) {
        erpMrpResultComponentMapper.deleteByPlanId(planId);
    }

    @Override
    public List<ErpMrpResultComponentDO> getListByResultId(Long resultId) {
        if (resultId == null) {
            return Collections.emptyList();
        }
        return erpMrpResultComponentMapper.selectListByResultId(resultId);
    }
}
