package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.production.ErpProductionOrderFinishReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.production.ErpProductionOrderPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.production.ErpProductionOrderSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionSuggestDO;

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
