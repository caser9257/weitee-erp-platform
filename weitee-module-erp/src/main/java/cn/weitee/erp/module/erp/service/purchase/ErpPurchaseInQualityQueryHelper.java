package cn.weitee.erp.module.erp.service.purchase;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualityPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityDefectDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityRoundDO;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInQualityDefectMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInQualityItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInQualityMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInQualityRoundMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_QUALITY_ORDER_NOT_EXISTS;

/**
 * 质检单查询辅助类。
 * 提取自 ErpPurchaseInQualityServiceImpl，供首检、复检、退货创建等流程共用。
 */
@Component
class ErpPurchaseInQualityQueryHelper {

    @Resource
    private ErpPurchaseInMapper erpPurchaseInMapper;
    @Resource
    private ErpPurchaseInQualityMapper erpPurchaseInQualityMapper;
    @Resource
    private ErpPurchaseInQualityItemMapper erpPurchaseInQualityItemMapper;
    @Resource
    private ErpPurchaseInQualityRoundMapper erpPurchaseInQualityRoundMapper;
    @Resource
    private ErpPurchaseInQualityDefectMapper erpPurchaseInQualityDefectMapper;

    /**
     * 获取采购入库单（不存在则抛异常）。
     */
    ErpPurchaseInDO getRequiredPurchaseIn(Long purchaseInId) {
        ErpPurchaseInDO purchaseIn = erpPurchaseInMapper.selectById(purchaseInId);
        if (purchaseIn == null) {
            throw exception(PURCHASE_IN_NOT_EXISTS);
        }
        return purchaseIn;
    }

    /**
     * 获取质检单（不存在则抛异常）。
     */
    ErpPurchaseInQualityDO getRequiredPurchaseInQuality(Long id) {
        ErpPurchaseInQualityDO quality = erpPurchaseInQualityMapper.selectById(id);
        if (quality == null) {
            throw exception(PURCHASE_IN_QUALITY_ORDER_NOT_EXISTS);
        }
        return quality;
    }

    /**
     * 获取质检单（可空）。
     */
    ErpPurchaseInQualityDO getPurchaseInQuality(Long id) {
        return erpPurchaseInQualityMapper.selectById(id);
    }

    /**
     * 按采购入库 ID 获取质检单（可空）。
     */
    ErpPurchaseInQualityDO getPurchaseInQualityByPurchaseInId(Long purchaseInId) {
        return erpPurchaseInQualityMapper.selectByPurchaseInId(purchaseInId);
    }

    /**
     * 按质检单 ID 获取质检明细列表。
     */
    List<ErpPurchaseInQualityItemDO> getQualityItemListByQualityId(Long qualityId) {
        return erpPurchaseInQualityItemMapper.selectListByQualityId(qualityId);
    }

    /**
     * 按质检单 ID 获取质检轮次列表。
     */
    List<ErpPurchaseInQualityRoundDO> getRoundDOListByQualityId(Long qualityId) {
        return erpPurchaseInQualityRoundMapper.selectListByQualityId(qualityId);
    }

    /**
     * 按质检单 ID 获取不良明细列表。
     */
    List<ErpPurchaseInQualityDefectDO> getDefectDOListByQualityId(Long qualityId) {
        return erpPurchaseInQualityDefectMapper.selectListByQualityId(qualityId);
    }

    /**
     * 分页查询质检单。
     */
    PageResult<ErpPurchaseInQualityDO> getPurchaseInQualityPage(ErpPurchaseInQualityPageReqVO pageReqVO) {
        return erpPurchaseInQualityMapper.selectPage(pageReqVO);
    }
}
