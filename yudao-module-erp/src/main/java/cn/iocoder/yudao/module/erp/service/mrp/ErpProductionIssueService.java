package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.issue.ErpProductionIssueCreateReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.issue.ErpProductionIssuePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.issue.ErpProductionIssueRecommendReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.issue.ErpProductionIssueRecommendRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionIssueBatchDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionIssueDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionIssueItemDO;

import javax.validation.Valid;
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
