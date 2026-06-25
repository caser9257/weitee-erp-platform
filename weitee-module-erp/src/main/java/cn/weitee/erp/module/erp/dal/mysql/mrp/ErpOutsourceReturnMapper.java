package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.outsource.ErpOutsourceReturnPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpOutsourceReturnDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpOutsourceReturnMapper extends BaseMapperX<ErpOutsourceReturnDO> {
    default PageResult<ErpOutsourceReturnDO> selectPage(ErpOutsourceReturnPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpOutsourceReturnDO>()
                .likeIfPresent(ErpOutsourceReturnDO::getReturnNo, reqVO.getReturnNo())
                .eqIfPresent(ErpOutsourceReturnDO::getOrderId, reqVO.getOrderId())
                .eqIfPresent(ErpOutsourceReturnDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpOutsourceReturnDO::getId));
    }
    default List<ErpOutsourceReturnDO> selectListByOrderId(Long orderId) {
        return selectList(ErpOutsourceReturnDO::getOrderId, orderId);
    }
}
