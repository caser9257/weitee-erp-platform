package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.issue.ErpProductionIssueCreateReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.issue.ErpProductionIssuePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.issue.ErpProductionIssueRecommendReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.issue.ErpProductionIssueRecommendRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionIssueBatchDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionIssueDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionIssueItemDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

public interface ErpProductionIssueService {

    ErpProductionIssueRecommendRespVO recommend(@Valid ErpProductionIssueRecommendReqVO reqVO);

    Long createProductionIssue(@Valid ErpProductionIssueCreateReqVO reqVO);

    ErpProductionIssueDO getProductionIssue(Long id);

    PageResult<ErpProductionIssueDO> getProductionIssuePage(@Valid ErpProductionIssuePageReqVO pageReqVO);

    List<ErpProductionIssueItemDO> getProductionIssueItemListByIssueId(Long issueId);

    List<ErpProductionIssueItemDO> getProductionIssueItemListByIssueIds(Collection<Long> issueIds);

    List<ErpProductionIssueBatchDO> getProductionIssueBatchListByIssueItemIds(Collection<Long> issueItemIds);

}
