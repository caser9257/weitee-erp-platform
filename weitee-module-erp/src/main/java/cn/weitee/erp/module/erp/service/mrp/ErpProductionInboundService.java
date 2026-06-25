package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.inbound.ErpProductionInboundPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionFinishQualityDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionInboundDO;

public interface ErpProductionInboundService {

    Long createProductionInboundFromQuality(ErpProductionFinishQualityDO quality);

    void executeProductionInbound(Long operatorUserId, Long id);

    void cancelProductionInbound(Long id);

    void revertProductionInbound(Long operatorUserId, Long id);

    ErpProductionInboundDO getProductionInbound(Long id);

    ErpProductionInboundDO getProductionInboundByFinishQualityId(Long finishQualityId);

    PageResult<ErpProductionInboundDO> getProductionInboundPage(ErpProductionInboundPageReqVO pageReqVO);

}
