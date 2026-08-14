package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.quality.ErpProductionStepQualityPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderStepDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionReportItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionStepQualityDO;

import java.math.BigDecimal;
import java.util.List;

public interface ErpProductionStepQualityService {

    /**
     * 报工后按明细创建待检工序质检单（仅工序要求质检时）。
     */
    void createPendingFromReport(Long reportId, List<ErpProductionReportItemDO> items,
                                 List<ErpProductionOrderStepDO> steps);

    void submitQuality(Long qualityId, Long checkerUserId, BigDecimal qualifiedQty,
                       BigDecimal unqualifiedQty, String remark);

    ErpProductionStepQualityDO getQuality(Long id);

    PageResult<ErpProductionStepQualityDO> getQualityPage(ErpProductionStepQualityPageReqVO pageReqVO);

}
