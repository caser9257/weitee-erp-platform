package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.suggest.ErpProductionSuggestConvertReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.suggest.ErpProductionSuggestPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.suggest.ErpPurchaseSuggestConvertReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.suggest.ErpPurchaseSuggestPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionSuggestDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpPurchaseSuggestDO;

import javax.validation.Valid;
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
