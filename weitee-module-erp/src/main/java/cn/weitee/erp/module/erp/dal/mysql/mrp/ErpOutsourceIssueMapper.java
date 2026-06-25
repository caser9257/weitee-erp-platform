package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.outsource.ErpOutsourceIssuePageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpOutsourceIssueDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpOutsourceIssueMapper extends BaseMapperX<ErpOutsourceIssueDO> {
    default PageResult<ErpOutsourceIssueDO> selectPage(ErpOutsourceIssuePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpOutsourceIssueDO>()
                .likeIfPresent(ErpOutsourceIssueDO::getIssueNo, reqVO.getIssueNo())
                .eqIfPresent(ErpOutsourceIssueDO::getOrderId, reqVO.getOrderId())
                .eqIfPresent(ErpOutsourceIssueDO::getIssueType, reqVO.getIssueType())
                .eqIfPresent(ErpOutsourceIssueDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpOutsourceIssueDO::getId));
    }
    default List<ErpOutsourceIssueDO> selectListByOrderId(Long orderId) {
        return selectList(ErpOutsourceIssueDO::getOrderId, orderId);
    }
}
