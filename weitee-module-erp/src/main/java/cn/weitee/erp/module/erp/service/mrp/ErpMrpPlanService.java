package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.plan.ErpMrpPlanPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.plan.ErpMrpPlanSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpPlanDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpResultComponentDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpResultDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpShortageDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderDO;

import jakarta.validation.Valid;
import java.util.List;

public interface ErpMrpPlanService {

    Long createPlan(@Valid ErpMrpPlanSaveReqVO createReqVO);

    Long createAutoPlanForSaleOrder(ErpSaleOrderDO saleOrder);

    void runPlan(Long id);

    ErpMrpPlanDO getPlan(Long id);

    PageResult<ErpMrpPlanDO> getPlanPage(ErpMrpPlanPageReqVO pageReqVO);

    List<ErpMrpResultDO> getResultList(Long planId);

    List<ErpMrpResultComponentDO> getResultComponentList(Long resultId);

    List<ErpMrpShortageDO> getShortageList(Long planId);

}
