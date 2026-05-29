package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.quality.ErpProductionFinishQualityPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionFinishQualityDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;

import java.math.BigDecimal;

public interface ErpProductionFinishQualityService {

    Long createPendingQualityAfterFinish(ErpProductionOrderDO order, BigDecimal reportQty);

    void submitQuality(Long qualityId, Long checkerUserId, BigDecimal qualifiedQty, BigDecimal unqualifiedQty, String remark);

    ErpProductionFinishQualityDO getProductionFinishQuality(Long id);

    PageResult<ErpProductionFinishQualityDO> getProductionFinishQualityPage(ErpProductionFinishQualityPageReqVO pageReqVO);

}
