package cn.iocoder.yudao.module.erp.service.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInQualityCheckReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualityAssignCheckerReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualityPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualityStartRecheckReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualitySubmitFirstCheckReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualitySubmitRecheckReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualitySubmitReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityDefectDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityRoundDO;

import java.util.List;

public interface ErpPurchaseInQualityService {

    Long createQualityOrderIfAbsent(Long purchaseInId);

    void voidQualityOrderByPurchaseIn(Long purchaseInId, String reason);

    void submitPurchaseInQuality(Long userId, ErpPurchaseInQualitySubmitReqVO reqVO);

    void submitPurchaseInQualityByPurchaseIn(Long userId, ErpPurchaseInQualityCheckReqVO reqVO);

    void assignChecker(Long userId, ErpPurchaseInQualityAssignCheckerReqVO reqVO);

    void submitFirstCheck(Long userId, ErpPurchaseInQualitySubmitFirstCheckReqVO reqVO);

    void startRecheck(Long userId, ErpPurchaseInQualityStartRecheckReqVO reqVO);

    void submitRecheck(Long userId, ErpPurchaseInQualitySubmitRecheckReqVO reqVO);

    ErpPurchaseInQualityDO getPurchaseInQuality(Long id);

    ErpPurchaseInQualityDO getPurchaseInQualityByPurchaseInId(Long purchaseInId);

    List<ErpPurchaseInQualityItemDO> getPurchaseInQualityItemListByQualityId(Long qualityId);

    List<ErpPurchaseInQualityRoundDO> getRoundDOListByQualityId(Long qualityId);

    List<ErpPurchaseInQualityDefectDO> getDefectDOListByQualityId(Long qualityId);

    PageResult<ErpPurchaseInQualityDO> getPurchaseInQualityPage(ErpPurchaseInQualityPageReqVO pageReqVO);

}
