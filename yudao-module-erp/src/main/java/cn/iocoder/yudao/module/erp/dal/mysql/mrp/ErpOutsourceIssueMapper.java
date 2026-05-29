package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.outsource.ErpOutsourceIssuePageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpOutsourceIssueDO;
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
