package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.issue.ErpProductionIssuePageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionIssueDO;
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
