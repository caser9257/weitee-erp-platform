package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.quality.ErpProductionFinishQualityPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionFinishQualityDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;

import java.math.BigDecimal;

public interface ErpProductionFinishQualityService {

    Long createPendingQualityAfterFinish(ErpProductionOrderDO order, BigDecimal reportQty);

    void submitQuality(Long qualityId, Long checkerUserId, BigDecimal qualifiedQty, BigDecimal unqualifiedQty, String remark);

    ErpProductionFinishQualityDO getProductionFinishQuality(Long id);

    PageResult<ErpProductionFinishQualityDO> getProductionFinishQualityPage(ErpProductionFinishQualityPageReqVO pageReqVO);

}
