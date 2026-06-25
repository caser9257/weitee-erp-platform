package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apestimate.ErpApEstimateActionReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apestimate.ErpApEstimatePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apestimate.ErpApEstimateScanReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApEstimateDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApEstimateItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApStatementDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

public interface ErpApEstimateService {

    int generateMonthEstimate(@Valid ErpApEstimateScanReqVO reqVO);

    void confirmApEstimate(Long userId, @Valid ErpApEstimateActionReqVO reqVO);

    void reverseApEstimate(Long userId, @Valid ErpApEstimateActionReqVO reqVO);

    void reverseBySourceBiz(Integer sourceBizType, Long sourceBizId, Long userId, String remark);

    void reverseBySourceBiz(Integer sourceBizType, Long sourceBizId, Long userId, Integer reverseType,
                            Long reverseSourceId, String reverseSourceNo, String remark);

    void syncByStatementInvoiceChange(ErpApStatementDO statement, Integer oldInvoiceStatus, Integer newInvoiceStatus,
                                      Long userId, Long reverseSourceId, String reverseSourceNo);

    ErpApEstimateDO getApEstimate(Long id);

    List<ErpApEstimateDO> getApEstimateListByIds(Collection<Long> ids);

    PageResult<ErpApEstimateDO> getApEstimatePage(ErpApEstimatePageReqVO pageReqVO);

    List<ErpApEstimateItemDO> getApEstimateItemListByEstimateId(Long estimateId);

    List<ErpApEstimateItemDO> getApEstimateItemListByEstimateIds(Collection<Long> estimateIds);

}
