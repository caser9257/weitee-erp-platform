package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.issue.ErpProductionIssuePageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionIssueDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpProductionIssueMapper extends BaseMapperX<ErpProductionIssueDO> {

    default PageResult<ErpProductionIssueDO> selectPage(ErpProductionIssuePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpProductionIssueDO>()
                .likeIfPresent(ErpProductionIssueDO::getIssueNo, reqVO.getIssueNo())
                .eqIfPresent(ErpProductionIssueDO::getProductionOrderId, reqVO.getProductionOrderId())
                .eqIfPresent(ErpProductionIssueDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ErpProductionIssueDO::getIssueTime, reqVO.getIssueTime())
                .orderByDesc(ErpProductionIssueDO::getId));
    }

    default List<ErpProductionIssueDO> selectListByProductionOrderId(Long productionOrderId) {
        return selectList(new LambdaQueryWrapperX<ErpProductionIssueDO>()
                .eq(ErpProductionIssueDO::getProductionOrderId, productionOrderId)
                .orderByAsc(ErpProductionIssueDO::getId));
    }

}
