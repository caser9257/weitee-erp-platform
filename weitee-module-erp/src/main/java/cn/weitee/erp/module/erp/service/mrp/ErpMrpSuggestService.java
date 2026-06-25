package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.suggest.ErpProductionSuggestConvertReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.suggest.ErpProductionSuggestPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.suggest.ErpPurchaseSuggestConvertReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.suggest.ErpPurchaseSuggestPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionSuggestDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpPurchaseSuggestDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

public interface ErpMrpSuggestService {

    PageResult<ErpPurchaseSuggestDO> getPurchaseSuggestPage(ErpPurchaseSuggestPageReqVO pageReqVO);

    PageResult<ErpProductionSuggestDO> getProductionSuggestPage(ErpProductionSuggestPageReqVO pageReqVO);

    void confirmPurchaseSuggest(List<Long> ids);

    void confirmProductionSuggest(List<Long> ids);

    void rejectPurchaseSuggest(List<Long> ids);

    void rejectProductionSuggest(List<Long> ids);

    Long convertPurchaseSuggest(@Valid ErpPurchaseSuggestConvertReqVO reqVO);

    List<Long> convertProductionSuggest(@Valid ErpProductionSuggestConvertReqVO reqVO);

    List<ErpPurchaseSuggestDO> getPurchaseSuggestListByConvertPurchaseOrderIds(Collection<Long> convertPurchaseOrderIds);

}
