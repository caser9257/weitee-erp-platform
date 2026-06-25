package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.production.ErpProductionOrderFinishReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.production.ErpProductionOrderPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.production.ErpProductionOrderSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionSuggestDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

public interface ErpProductionOrderService {

    Long createProductionOrder(@Valid ErpProductionOrderSaveReqVO createReqVO);

    Long createProductionOrderBySuggest(ErpProductionSuggestDO suggest, String remark);

    void updateProductionOrder(@Valid ErpProductionOrderSaveReqVO updateReqVO);

    void releaseProductionOrder(Long id);

    void finishProductionOrder(@Valid ErpProductionOrderFinishReqVO reqVO);

    ErpProductionOrderDO getProductionOrder(Long id);

    List<ErpProductionOrderDO> getProductionOrderList(Collection<Long> ids);

    PageResult<ErpProductionOrderDO> getProductionOrderPage(ErpProductionOrderPageReqVO pageReqVO);

}
