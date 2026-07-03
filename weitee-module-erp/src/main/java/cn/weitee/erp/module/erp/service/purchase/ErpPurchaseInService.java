package cn.weitee.erp.module.erp.service.purchase;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInBatchUpdateReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInBatchUpdateResultVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInConfirmStockInReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInQualityCheckReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInStockExecuteCreateReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInStockExecuteDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInStockExecuteItemBatchDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInStockExecuteItemDO;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * ERP 采购入库 Service 接口
 */
public interface ErpPurchaseInService {

    Long createPurchaseIn(@Valid ErpPurchaseInSaveReqVO createReqVO);

    void updatePurchaseIn(@Valid ErpPurchaseInSaveReqVO updateReqVO);

    ErpPurchaseInBatchUpdateResultVO updatePurchaseInBatch(@Valid ErpPurchaseInBatchUpdateReqVO reqVO);

    void updatePurchaseInStatus(Long id, Integer status);

    void updatePurchaseInStatusByBpm(Long id, String processInstanceId, Integer status, String reason);

    void rollbackPurchaseInStatusToDraftByBpm(Long id, String processInstanceId, String reason);

    void qualityCheckPurchaseIn(Long userId, @Valid ErpPurchaseInQualityCheckReqVO reqVO);

    void confirmPurchaseInStockIn(Long userId, @Valid ErpPurchaseInConfirmStockInReqVO reqVO);

    void createPurchaseInStockExecute(Long userId, @Valid ErpPurchaseInStockExecuteCreateReqVO reqVO);

    void updatePurchaseInPaymentPrice(Long id, BigDecimal paymentPrice);

    void deletePurchaseIn(List<Long> ids);

    ErpPurchaseInDO getPurchaseIn(Long id);

    ErpPurchaseInDO validatePurchaseIn(Long id);

    PageResult<ErpPurchaseInDO> getPurchaseInPage(ErpPurchaseInPageReqVO pageReqVO);

    List<ErpPurchaseInDO> getPurchaseInListByIds(Collection<Long> ids);

    List<ErpPurchaseInDO> getPurchaseInListByOrderIds(Collection<Long> orderIds);

    List<ErpPurchaseInItemDO> getPurchaseInItemListByInId(Long inId);

    List<ErpPurchaseInItemDO> getPurchaseInItemListByInIds(Collection<Long> inIds);

    List<ErpPurchaseInStockExecuteDO> getPurchaseInStockExecuteListByPurchaseInId(Long purchaseInId);

    List<ErpPurchaseInStockExecuteDO> getPurchaseInStockExecuteListByIds(Collection<Long> ids);

    List<ErpPurchaseInStockExecuteItemDO> getPurchaseInStockExecuteItemListByExecuteIds(Collection<Long> executeIds);

    List<ErpPurchaseInStockExecuteItemDO> getPurchaseInStockExecuteItemListByIds(Collection<Long> ids);

    List<ErpPurchaseInStockExecuteItemBatchDO> getPurchaseInStockExecuteItemBatchListByExecuteItemIds(Collection<Long> executeItemIds);

    List<ErpPurchaseInStockExecuteItemBatchDO> getPurchaseInStockExecuteItemBatchListByPurchaseSourceBatchId(Long purchaseSourceBatchId);

}
