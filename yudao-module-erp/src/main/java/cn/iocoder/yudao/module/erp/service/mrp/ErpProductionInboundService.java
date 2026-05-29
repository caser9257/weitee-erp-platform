package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.inbound.ErpProductionInboundPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionFinishQualityDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionInboundDO;

public interface ErpProductionInboundService {

    Long createProductionInboundFromQuality(ErpProductionFinishQualityDO quality);

    void executeProductionInbound(Long operatorUserId, Long id);

    void cancelProductionInbound(Long id);

    void revertProductionInbound(Long operatorUserId, Long id);

    ErpProductionInboundDO getProductionInbound(Long id);

    ErpProductionInboundDO getProductionInboundByFinishQualityId(Long finishQualityId);

    PageResult<ErpProductionInboundDO> getProductionInboundPage(ErpProductionInboundPageReqVO pageReqVO);

}
