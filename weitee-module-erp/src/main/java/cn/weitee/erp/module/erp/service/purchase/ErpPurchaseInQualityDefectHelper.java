package cn.weitee.erp.module.erp.service.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualitySubmitFirstCheckReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.inquality.ErpPurchaseInQualitySubmitRecheckReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityDefectDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityItemDO;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInQualityDefectMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 质检不良明细写入辅助类。
 * 提取自 ErpPurchaseInQualityServiceImpl，供首检、复检流程共用。
 */
@Component
class ErpPurchaseInQualityDefectHelper {

    @Resource
    private ErpPurchaseInQualityDefectMapper erpPurchaseInQualityDefectMapper;

    /**
     * 写入初检不良明细。
     */
    void insertFirstCheckDefects(Long roundId, Long qualityId, ErpPurchaseInQualityItemDO qualityItem,
                                 List<ErpPurchaseInQualitySubmitFirstCheckReqVO.Defect> defects) {
        if (CollUtil.isEmpty(defects)) {
            return;
        }
        List<ErpPurchaseInQualityDefectDO> defectDOList = convertList(defects, defect ->
                new ErpPurchaseInQualityDefectDO()
                        .setQualityId(qualityId)
                        .setRoundId(roundId)
                        .setQualityItemId(qualityItem.getId())
                        .setPurchaseInItemId(qualityItem.getPurchaseInItemId())
                        .setDefectReasonId(defect.getDefectReasonId())
                        .setDefectReasonName(defect.getDefectReasonName())
                        .setDefectCount(defect.getDefectCount())
                        .setRemark(defect.getDefectRemark()));
        erpPurchaseInQualityDefectMapper.insertBatch(defectDOList);
    }

    /**
     * 写入复检不良明细。
     */
    void insertRecheckDefects(Long roundId, Long qualityId, ErpPurchaseInQualityItemDO qualityItem,
                              List<ErpPurchaseInQualitySubmitRecheckReqVO.Defect> defects) {
        if (CollUtil.isEmpty(defects)) {
            return;
        }
        List<ErpPurchaseInQualityDefectDO> defectDOList = convertList(defects, defect ->
                new ErpPurchaseInQualityDefectDO()
                        .setQualityId(qualityId)
                        .setRoundId(roundId)
                        .setQualityItemId(qualityItem.getId())
                        .setPurchaseInItemId(qualityItem.getPurchaseInItemId())
                        .setDefectReasonId(defect.getDefectReasonId())
                        .setDefectReasonName(defect.getDefectReasonName())
                        .setDefectCount(defect.getDefectCount())
                        .setRemark(defect.getDefectRemark()));
        erpPurchaseInQualityDefectMapper.insertBatch(defectDOList);
    }
}
